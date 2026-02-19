package icu.windea.pls.lang.util

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.cache.trackedBy
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.findChild
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.argumentNames
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.contextKey
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.codeInsight.completion.parameters
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.withPatchableIcon
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.resolve.expression.ParadoxParameterConditionExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ParadoxParameterInfo
import icu.windea.pls.model.toInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionExpression
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import java.util.*

object ParadoxParameterManager {
    object Keys : KeyRegistry() {
        val cachedParameterContextInfo by registerKey<CachedValue<ParadoxParameterContextInfo>>(Keys)
        val inferredContextConfigsFromUsages by registerKey<List<CwtMemberConfig<*>>>(Keys)
    }

    private val CwtConfigGroup.parameterInfoCache by registerKey(CwtConfigGroup.Keys) {
        // rootFile -> cacheKey -> parameterInfo
        createNestedCache<VirtualFile, _, _, Cache<String, ParadoxParameterInfo>> {
            CacheBuilder().build<String, ParadoxParameterInfo>().cancelable().trackedBy { it.modificationTracker }
        }
    }

    /**
     * 得到 [element] 的文本，然后使用指定的一组 [args] 替换其中的占位符。
     *
     * 如果 [direct] 为 `true`，则直接将占位符 `$P$` 替换成传入参数 `P` 的值。此时：
     * - 值可以是多行字符串。
     * - 如果值是用双引号括起，替换时会被忽略。
     * - 允许重复的传入参数，按顺序进行替换。
     *
     * @param element 用于得到原始文本的 PSI。
     * @param args 传入参数的键值对。如果值是用双引号括起的，需要保留。
     */
    fun replaceTextWithArgs(element: PsiElement, args: List<Tuple2<String, String>>, direct: Boolean): String {
        if (direct) {
            val oldText = element.text
            var newText = oldText
            args.forEach { (k, v) ->
                newText = newText.replace("$$k$", v.unquote())
            }
            return newText
        } else {
            val offset = element.startOffset
            val argMap = args.toMap()
            val replacements = mutableListOf<Tuple2<TextRange, String>>()

            element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun elementFinished(element: PsiElement) {
                    run {
                        if (element !is ParadoxScriptParameterCondition) return@run
                        val conditionExpression = element.parameterConditionExpression ?: return@run
                        val parameter = conditionExpression.parameterConditionParameter
                        val name = parameter.name
                        val v = argMap[name] ?: return@run
                        val revert = v.equals("no", true)
                        val operator = conditionExpression.findChild { it.elementType == ParadoxScriptElementTypes.NOT_SIGN } == null
                        if ((!revert && operator) || (revert && !operator)) {
                            val (start, end) = ParadoxPsiManager.findMemberElementsToInline(element)
                            if (start != null && end != null) {
                                element.parent.addRangeAfter(start, end, element)
                            }
                        }
                        element.delete()
                    }
                }
            })

            element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    run {
                        if (element !is ParadoxParameter) return@run
                        val n = element.name ?: return@run
                        val v0 = argMap[n] ?: return@run
                        val v = v0
                        replacements.add(tupleOf(element.textRange.shiftLeft(offset), v))
                        return
                    }
                    super.visitElement(element)
                }
            })

            var newText = element.text
            replacements.reversed().forEach { (range, v) ->
                newText = newText.replaceRange(range.startOffset, range.endOffset, v)
            }
            return newText
        }
    }

    /**
     * 得到 [element] 对应的参数上下文信息。
     *
     * 这个方法不会判断 [element] 是否是合法的参数上下文，如果需要，考虑使用 [ParadoxParameterSupport.getContextInfo]。
     */
    fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterContextInfo) {
            val value = doGetContextInfo(element)
            CachedValueProvider.Result(value, element)
        }
    }

    private fun doGetContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        val file = element.containingFile
        val gameType = selectGameType(file) ?: return null
        val parameters = sortedMapOf<String, MutableList<ParadoxParameterContextInfo.Parameter>>() // 按名字进行排序
        val fileConditionExpressions = ArrayDeque<ParadoxParameterConditionExpression>()
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptParameterConditionExpression) return visitParameterConditionExpression(element)
                if (element is ParadoxConditionParameter) return visitConditionParameter(element)
                if (element is ParadoxParameter) return visitParameter(element)
                super.visitElement(element)
            }

            private fun visitParameterConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                // value may be empty (invalid condition expression)
                fileConditionExpressions.addLast(ParadoxParameterConditionExpression.resolve(element.text))
                super.visitElement(element)
            }

            private fun visitConditionParameter(element: ParadoxConditionParameter) {
                val name = element.name ?: return
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, null, null)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            private fun visitParameter(element: ParadoxParameter) {
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalExpressions = ArrayDeque(fileConditionExpressions) // not null
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, defaultValue, conditionalExpressions)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptParameterCondition || element is ParadoxScriptInlineParameterCondition) {
                    fileConditionExpressions.removeLast()
                }
            }
        })
        return ParadoxParameterContextInfo(parameters, file.project, gameType)
    }

    /**
     * 基于指定的参数上下文信息以及输入的一组参数，判断指定名字的参数是否是可选的。
     */
    fun isOptional(parameterContextInfo: ParadoxParameterContextInfo, parameterName: String, argumentNames: Set<String>? = null): Boolean {
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if (parameterInfos.isNullOrEmpty()) return true
        return parameterInfos.all { parameterInfo -> isOptional(parameterInfo, argumentNames) }
    }

    /**
     * 基于指定的参数信息以及输入的一组参数，判断此参数是否是可选的。
     */
    fun isOptional(parameterInfo: ParadoxParameterContextInfo.Parameter, argumentNames: Set<String>? = null): Boolean {
        // 如果带有默认值，则为可选
        if (parameterInfo.defaultValue != null) return true
        // 如果是条件参数，则为可选
        if (parameterInfo.conditionExpressions == null) return true
        // 如果从参数条件表达式的堆栈来看是可选的，则为可选
        if (isOptionalFromConditionExpressions(parameterInfo, argumentNames)) return true
        // 如果作为传入参数的值，则认为是可选的
        if (isPassingParameterValue(parameterInfo)) return true
        return false
    }

    fun isOptionalFromConditionExpressions(parameterInfo: ParadoxParameterContextInfo.Parameter, argumentNames: Set<String>?): Boolean {
        val conditionExpressions = parameterInfo.conditionExpressions
        if (conditionExpressions.isNullOrEmpty()) return false
        return !conditionExpressions.all { it.matches(argumentNames) }
    }

    fun isPassingParameterValue(parameterInfo: ParadoxParameterContextInfo.Parameter): Boolean {
        val expressionConfigs = getExpressionConfigs(parameterInfo)
        return expressionConfigs.any { it is CwtValueConfig && it.propertyConfig?.configExpression?.type == CwtDataTypes.Parameter }
    }

    /**
     * 基于指定的参数信息，得到对应的脚本表达式所对应的上下文规则列表。此参数需要整个作为一个脚本表达式。
     */
    fun getExpressionContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter): List<CwtMemberConfig<*>> {
        val expressionElement = parameterInfo.parentElement?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return emptyList()
        if (!expressionElement.value.isParameterized(full = true)) return emptyList()
        val expressionContextConfigs = ParadoxConfigManager.getConfigContext(expressionElement)?.getConfigs()
        return expressionContextConfigs.orEmpty()
    }

    /**
     * 基于指定的参数信息，得到对应的脚本表达式所对应的规则列表。此参数可能整个作为一个脚本表达式，或者被一个脚本表达式所包含。
     */
    fun getExpressionConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter): List<CwtMemberConfig<*>> {
        val expressionElement = parameterInfo.parentElement.castOrNull<ParadoxScriptStringExpressionElement>() ?: return emptyList()
        return when {
            expressionElement is ParadoxScriptPropertyKey -> {
                val configs = ParadoxConfigManager.getConfigs(expressionElement)
                configs.filterIsInstance<CwtPropertyConfig>()
            }
            expressionElement is ParadoxScriptString && expressionElement.isExpression() -> {
                val configs = ParadoxConfigManager.getConfigs(expressionElement)
                configs.filterIsInstance<CwtValueConfig>()
            }
            else -> {
                emptyList()
            }
        }
    }

    fun getRequiredParameterNames(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, argumentNames: MutableSet<String>): MutableSet<String> {
        val result = mutableSetOf<String>()
        ParadoxParameterService.processContextReference(element, contextReferenceInfo, true) p@{
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterService.getContextInfo(it) ?: return@p true
            if (parameterContextInfo.parameters.isEmpty()) return@p true
            for (parameterName in parameterContextInfo.parameters.keys) {
                if (result.contains(parameterName)) continue
                if (!isOptional(parameterContextInfo, parameterName, argumentNames)) result.add(parameterName)
            }
            false
        }
        return result
    }

    fun getPresentArgumentNames(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo): MutableSet<String> {
        val result = mutableSetOf<String>()
        for (argument in contextReferenceInfo.arguments) {
            if (!isPresent(element, argument, contextReferenceInfo.project)) continue
            result.add(argument.argumentName)
        }
        return result
    }

    fun isPresent(element: PsiElement, argumentInfo: ParadoxParameterContextReferenceInfo.Argument, project: Project): Boolean {
        val argumentValue = argumentInfo.argumentValue ?: return true
        val resolved = ParadoxExpressionManager.resolve(argumentValue, element, project)
        return resolved != "no"
    }

    fun completeParameters(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        // 向上找到参数上下文
        val parameterContext = ParadoxParameterService.findContext(element) ?: return
        val parameterContextInfo = ParadoxParameterService.getContextInfo(parameterContext) ?: return
        if (parameterContextInfo.parameters.isEmpty()) return
        for ((parameterName, parameterInfos) in parameterContextInfo.parameters) {
            ProgressManager.checkCanceled()
            val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
            // 排除当前正在输入的那个
            if (parameterInfos.size == 1 && element isSamePosition parameter) continue
            val parameterElement = when {
                parameter is ParadoxConditionParameter -> ParadoxParameterService.resolveConditionParameter(parameter)
                parameter is ParadoxParameter -> ParadoxParameterService.resolveParameter(parameter)
                else -> null
            } ?: continue
            val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                .withTypeText(parameterElement.contextName, parameterElement.contextIcon, true)
                .withPatchableIcon(PlsIcons.Nodes.Parameter)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }

        val contextKey = ParadoxParameterService.getContextKeyFromContext(parameterContext) ?: return
        context.contextKey = contextKey
        ParadoxExtendedCompletionManager.completeExtendedParameter(context, result)
    }

    fun completeArguments(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.quoted) return // 输入参数不允许用引号括起
        val from = ParadoxParameterContextReferenceInfo.From.Argument
        val config = context.config ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterService.getContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = mutableSetOf<String>()
        for (argument in contextReferenceInfo.arguments) {
            argumentNames.add(argument.argumentName)
        }
        // 整合查找到的所有参数上下文
        ParadoxParameterService.processContextReference(element, contextReferenceInfo, true) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterService.getContextInfo(parameterContext) ?: return@p true
            if (parameterContextInfo.parameters.isEmpty()) return@p true
            for ((parameterName, parameterInfos) in parameterContextInfo.parameters) {
                // 排除已输入的
                if (!argumentNames.add(parameterName)) continue

                val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
                val parameterElement = when {
                    parameter is ParadoxConditionParameter -> ParadoxParameterService.resolveConditionParameter(parameter)
                    parameter is ParadoxParameter -> ParadoxParameterService.resolveParameter(parameter)
                    else -> null
                } ?: continue
                val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                    .withTypeText(parameterElement.contextName, parameterElement.contextIcon, true)
                    .withPatchableIcon(PlsIcons.Nodes.Parameter)
                    .forScriptExpression(context)
                result.addElement(lookupElement, context)
            }
            true
        }

        context.contextKey = contextReferenceInfo.contextKey
        context.argumentNames = argumentNames
        ParadoxExtendedCompletionManager.completeExtendedParameter(context, result)
    }

    fun getReadWriteAccess(element: PsiElement): ReadWriteAccessDetector.Access {
        return when {
            element is ParadoxParameter -> ReadWriteAccessDetector.Access.Read
            element is ParadoxConditionParameter -> ReadWriteAccessDetector.Access.Read
            else -> ReadWriteAccessDetector.Access.Write
        }
    }

    fun getParameterElement(element: PsiElement): ParadoxParameterElement? {
        return when (element) {
            is ParadoxParameterElement -> element
            is ParadoxParameter -> ParadoxParameterService.resolveParameter(element)
            is ParadoxConditionParameter -> ParadoxParameterService.resolveConditionParameter(element)
            else -> null
        }
    }

    fun getParameterInfo(parameterElement: ParadoxParameterElement): ParadoxParameterInfo? {
        val rootFile = selectRootFile(parameterElement) ?: return null
        val project = parameterElement.project
        val configGroup = PlsFacade.getConfigGroup(project, parameterElement.gameType)
        val cache = configGroup.parameterInfoCache.get(rootFile)
        val cacheKey = "${parameterElement.name}@${parameterElement.contextKey}"
        val parameterInfo = cache.get(cacheKey) {
            parameterElement.toInfo()
        }
        return parameterInfo
    }

    /**
     * 尝试推断得到参数的上下文规则。基于用法和扩展规则。
     */
    fun getInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val inferenceSettings = PlsSettings.getInstance().state.inference
        if (!inferenceSettings.configContextForParameters) return emptyList()
        val fast = inferenceSettings.configContextForParametersFast

        if (inferenceSettings.configContextForParametersFromConfig) {
            val fromConfig = ParadoxParameterService.getInferredContextConfigsFromConfig(parameterElement, fast)
            if (fromConfig.isNotEmpty()) return fromConfig
        }

        if (inferenceSettings.configContextForParametersFromUsages) {
            val parameterInfo = getParameterInfo(parameterElement) ?: return emptyList()
            val fromUsages = parameterInfo.getOrPutUserData(Keys.inferredContextConfigsFromUsages) {
                ProgressManager.checkCanceled()
                ParadoxParameterService.getInferredContextConfigsFromUsages(parameterElement, fast)
            }
            if (fromUsages.isNotEmpty()) return fromUsages
        }

        return emptyList()
    }

    /**
     * 尝试推断得到参数的上下文规则。基于扩展规则。
     */
    fun getInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val inferenceSettings = PlsSettings.getInstance().state.inference
        if (!inferenceSettings.configContextForParameters) return emptyList()
        val fast = inferenceSettings.configContextForParametersFast

        return ParadoxParameterService.getInferredContextConfigsFromConfig(parameterElement, fast)
    }

    /**
     * 尝试推断得到参数的类型。仅用于显示。
     */
    fun getInferredType(parameterElement: ParadoxParameterElement): String? {
        val contextConfigs = getInferredContextConfigs(parameterElement)
        return ParadoxParameterService.getInferredType(contextConfigs)
    }

    /**
     * 尝试推断得到带参数的键对应的匹配的规则。
     */
    fun getParameterizedKeyConfigs(element: ParadoxScriptProperty): List<CwtValueConfig> {
        val propertyKey = element.propertyKey
        val parameter = propertyKey.findChild<ParadoxParameter>() ?: return emptyList()
        val parameterElement = getParameterElement(parameter) ?: return emptyList()
        val contextConfigs = getInferredContextConfigsFromConfig(parameterElement)
        return ParadoxParameterService.getParameterizedKeyConfigs(contextConfigs)
    }
}
