package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.config.matchFromPattern
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.findChild
import icu.windea.pls.core.getShreds
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.mergeValue
import icu.windea.pls.core.processChild
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.createNestedCache
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.trackedBy
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.parameter.ParadoxParameterInferredConfigProvider
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.argumentNames
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.contextKey
import icu.windea.pls.lang.codeInsight.completion.forScriptExpression
import icu.windea.pls.lang.codeInsight.completion.parameters
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.withPatchableIcon
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.elementInfo.ParadoxParameterInfo
import icu.windea.pls.model.elementInfo.toInfo
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionExpression
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.util.*

object ParadoxParameterManager {
    object Keys : KeyRegistry() {
        val cachedParameterContextInfo by createKey<CachedValue<ParadoxParameterContextInfo>>(Keys)
        val inferredContextConfigsFromUsages by createKey<List<CwtMemberConfig<*>>>(Keys)
        val parameterValueInjectionInfos by createKey<List<ParadoxParameterValueInjectionInfo>>(Keys)
    }

    //rootFile -> cacheKey -> parameterInfo
    //depends on config group
    private val CwtConfigGroup.parameterInfoCache by createKey(CwtConfigGroup.Keys) {
        createNestedCache<VirtualFile, String, ParadoxParameterInfo, com.github.benmanes.caffeine.cache.Cache<String, ParadoxParameterInfo>> {
            CacheBuilder().build<String, ParadoxParameterInfo>().cancelable().trackedBy { it.modificationTracker }
        }
    }

    /**
     * 得到[element]的文本，然后使用指定的一组[args]替换其中的占位符。
     *
     * 如果[direct]为true，则直接将占位符`$P$`替换成传入参数`P`的值。此时：
     *
     * - 值可以是多行字符串。
     * - 如果值是用双引号括起，替换时会被忽略。
     * - 允许重复的传入参数，按顺序进行替换。
     *
     * @param element 用于得到原始文本的PSI。
     * @param args 传入参数的键值对。如果值是用双引号括起的，需要保留。
     */
    fun replaceTextWithArgs(element: PsiElement, args: List<Tuple2<String, String>>, direct: Boolean): String {
        if (direct) {
            val oldText = element.text
            var newText = oldText
            args.forEach { (k,v) ->
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
     * 得到[element]对应的参数上下文信息。
     *
     * 这个方法不会判断[element]是否是合法的参数上下文，如果需要，考虑使用[ParadoxParameterSupport.getContextInfo]。
     */
    fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterContextInfo) {
            val value = doGetContextInfo(element)
            CachedValueProvider.Result(value, element)
        }
    }

    private fun doGetContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        val file = element.containingFile
        val gameType = selectGameType(file) ?: return null
        val parameters = sortedMapOf<String, MutableList<ParadoxParameterContextInfo.Parameter>>() //按名字进行排序
        val fileConditionStack = ArrayDeque<ReversibleValue<String>>()
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptParameterConditionExpression) visitParadoxConditionExpression(element)
                if (element is ParadoxConditionParameter) visitConditionParameter(element)
                if (element is ParadoxParameter) visitParameter(element)
                super.visitElement(element)
            }

            private fun visitParadoxConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                var operator = true
                var value = ""
                element.processChild p@{
                    val elementType = it.elementType
                    when (elementType) {
                        ParadoxScriptElementTypes.NOT_SIGN -> operator = false
                        ParadoxScriptElementTypes.PARAMETER_CONDITION_PARAMETER -> value = it.text
                    }
                    true
                }
                //value may be empty (invalid condition expression)
                fileConditionStack.addLast(ReversibleValue(operator, value))
                super.visitElement(element)
            }

            private fun visitConditionParameter(element: ParadoxConditionParameter) {
                val name = element.name ?: return
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, null, null)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                //不需要继续向下遍历
            }

            private fun visitParameter(element: ParadoxParameter) {
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalStack = ArrayDeque(fileConditionStack) //not null
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, defaultValue, conditionalStack)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                //不需要继续向下遍历
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptParameterCondition || element is ParadoxScriptInlineParameterCondition) {
                    fileConditionStack.removeLast()
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
        return parameterInfos.all f@{ parameterInfo ->
            //如果带有默认值，则为可选
            if (parameterInfo.defaultValue != null) return@f true
            //如果是条件参数，则为可选
            if (parameterInfo.conditionStack == null) return@f true
            //如果基于条件表达式上下文是可选的，则为可选
            if (parameterInfo.conditionStack.isNotEmpty() && parameterInfo.conditionStack
                    .all { it.withOperator { n -> parameterName == n || (argumentNames != null && argumentNames.contains(n)) } }
            ) return@f true
            //如果作为传入参数的值，则认为是可选的
            if (parameterInfo.expressionConfigs
                    .any { it is CwtValueConfig && it.propertyConfig?.configExpression?.type == CwtDataTypes.Parameter }
            ) return@f true
            false
        }
    }

    fun completeParameters(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        //向上找到参数上下文
        val parameterContext = ParadoxParameterSupport.findContext(element) ?: return
        val parameterContextInfo = ParadoxParameterSupport.getContextInfo(parameterContext) ?: return
        if (parameterContextInfo.parameters.isEmpty()) return
        for ((parameterName, parameterInfos) in parameterContextInfo.parameters) {
            ProgressManager.checkCanceled()
            val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
            //排除当前正在输入的那个
            if (parameterInfos.size == 1 && element isSamePosition parameter) continue
            val parameterElement = when {
                parameter is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(parameter)
                parameter is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(parameter)
                else -> null
            } ?: continue
            val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                .withTypeText(parameterElement.contextName, parameterElement.contextIcon, true)
                .withPatchableIcon(PlsIcons.Nodes.Parameter)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }

        val contextKey = ParadoxParameterSupport.getContextKeyFromContext(parameterContext) ?: return
        context.contextKey = contextKey
        ParadoxCompletionManager.completeExtendedParameter(context, result)
    }

    fun completeArguments(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.quoted) return //输入参数不允许用引号括起
        val from = ParadoxParameterContextReferenceInfo.From.Argument
        val config = context.config ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
        //整合查找到的所有参数上下文
        ParadoxParameterSupport.processContextReference(element, contextReferenceInfo, true) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterSupport.getContextInfo(parameterContext) ?: return@p true
            if (parameterContextInfo.parameters.isEmpty()) return@p true
            for ((parameterName, parameterInfos) in parameterContextInfo.parameters) {
                //排除已输入的
                if (!argumentNames.add(parameterName)) continue

                val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
                val parameterElement = when {
                    parameter is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(parameter)
                    parameter is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(parameter)
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
        ParadoxCompletionManager.completeExtendedParameter(context, result)
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
            is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(element)
            is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(element)
            else -> null
        }
    }

    fun getParameterInfo(parameterElement: ParadoxParameterElement): ParadoxParameterInfo? {
        val rootFile = selectRootFile(parameterElement) ?: return null
        val project = parameterElement.project
        val configGroup = PlsFacade.getConfigGroup(project, parameterElement.gameType)
        val cache = configGroup.parameterInfoCache.get(rootFile)
        val cacheKey = parameterElement.name + "@" + parameterElement.contextKey
        val parameterInfo = cache.get(cacheKey) {
            parameterElement.toInfo()
        }
        return parameterInfo
    }

    /**
     * 尝试推断得到参数的类型（仅用于显示）。
     */
    fun getInferredType(parameterElement: ParadoxParameterElement): String? {
        val contextConfigs = getInferredContextConfigs(parameterElement)
        if (contextConfigs.isEmpty()) return null
        val configs = contextConfigs.singleOrNull()?.configs
        if (configs.isNullOrEmpty()) return null
        if (configs.any { it !is CwtValueConfig || it.valueType == CwtType.Block }) return PlsBundle.message("complex")
        return configs.mapTo(mutableSetOf()) { it.configExpression.expressionString }.joinToString(" | ")
    }

    /**
     * 尝试推断得到参数对应的上下文CWT规则。
     */
    fun getInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val fromConfig = getInferredContextConfigsFromConfig(parameterElement)
        if (fromConfig.isNotEmpty()) return fromConfig

        if (!PlsFacade.getSettings().inference.configContextForParameters) return emptyList()
        val parameterInfo = getParameterInfo(parameterElement) ?: return emptyList()
        return parameterInfo.getOrPutUserData(Keys.inferredContextConfigsFromUsages) {
            ProgressManager.checkCanceled()
            withRecursionGuard {
                withRecursionCheck(parameterElement) {
                    doGetInferredContextConfigsFromUsages(parameterElement)
                }
            } ?: emptyList()
        }
    }

    /**
     * 尝试（从扩展的CWT规则）推断得到参数对应的上下文CWT规则。
     */
    fun getInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        return doGetInferredContextConfigsFromConfig(parameterElement)
    }

    private fun doGetInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val configGroup = PlsFacade.getConfigGroup(parameterElement.project, parameterElement.gameType)
        val configs = configGroup.extendedParameters.findFromPattern(parameterElement.name, parameterElement, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchFromPattern(parameterElement.contextKey, parameterElement, configGroup) } ?: return emptyList()
        return config.getContextConfigs(parameterElement)
    }

    private fun doGetInferredContextConfigsFromUsages(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val fastInference = PlsFacade.getSettings().inference.configContextForParametersFast
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        ParadoxParameterSupport.processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = ParadoxParameterSupport.getContextInfo(context) ?: return@p true
            val contextConfigs = doGetInferredContextConfigsFromUsages(parameterElement.name, contextInfo).orNull()
            if(fastInference && contextConfigs.isNotNullOrEmpty()) {
                result.set(contextConfigs)
                return@p false
            }
            result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
        }
        return result.get().orEmpty()
    }

    private fun doGetInferredContextConfigsFromUsages(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>> {
        val fastInference = PlsFacade.getSettings().inference.configContextForParametersFast
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if (parameterInfos.isNullOrEmpty()) return emptyList()
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        parameterInfos.process p@{ parameterInfo ->
            ProgressManager.checkCanceled()
            val contextConfigs = ParadoxParameterInferredConfigProvider.getContextConfigs(parameterInfo, parameterContextInfo).orNull()
            if(fastInference && contextConfigs.isNotNullOrEmpty()) {
                result.set(contextConfigs)
                return@p false
            }
            result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
        }
        return result.get().orEmpty()
    }

    fun getParameterizedKeyConfigs(element: PsiElement): List<CwtValueConfig> {
        val parameterizedProperty = element
        val propertyKey = parameterizedProperty.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return emptyList()
        val parameter = propertyKey.findChild<ParadoxParameter>() ?: return emptyList()
        val parameterElement = getParameterElement(parameter) ?: return emptyList()
        val contextConfigs = getInferredContextConfigsFromConfig(parameterElement)
        val configs = contextConfigs.singleOrNull()?.configs
            ?.filterNot { it !is CwtValueConfig || it.valueType == CwtType.Block }
        if (configs.isNullOrEmpty()) return emptyList()
        return configs.cast()
    }

    fun getParameterValueInjectionInfoFromInjectedFile(injectedFile: PsiFile): ParadoxParameterValueInjectionInfo? {
        val vFile = selectFile(injectedFile) ?: return null
        if (!PlsVfsManager.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host == null) return null

        val injectionInfos = host.getUserData(Keys.parameterValueInjectionInfos)
        if (injectionInfos.isNullOrEmpty()) return null
        val injectionInfo = when {
            host is ParadoxScriptStringExpressionElement -> {
                val file0 = vFile.toPsiFile(injectedFile.project) ?: injectedFile //actual PsiFile of VirtualFileWindow
                val shreds = file0.getShreds()
                val shred = shreds?.singleOrNull()
                val rangeInsideHost = shred?.rangeInsideHost ?: return null
                //it.rangeInsideHost may not equal to rangeInsideHost, but inside (e.g., there are escaped double quotes)
                injectionInfos.find { it.rangeInsideHost.startOffset in rangeInsideHost }
            }
            host is ParadoxParameter -> {
                //just use the only one
                injectionInfos.singleOrNull()
            }
            else -> null
        }
        return injectionInfo
    }
}
