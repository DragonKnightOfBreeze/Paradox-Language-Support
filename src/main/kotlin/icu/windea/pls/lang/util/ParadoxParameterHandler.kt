package icu.windea.pls.lang.util

import com.google.common.cache.*
import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.highlighting.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("UNUSED_PARAMETER")
object ParadoxParameterHandler {
    /**
     * 得到[element]对应的参数上下文信息。
     *
     * 这个方法不会判断[element]是否是合法的参数上下文，如果需要，考虑使用[ParadoxParameterSupport.getContextInfo]。
     */
    fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedParameterContextInfo) {
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
                if(element is ParadoxScriptParameterConditionExpression) visitParadoxConditionExpression(element)
                if(element is ParadoxConditionParameter) visitConditionParameter(element)
                if(element is ParadoxParameter) visitParameter(element)
                super.visitElement(element)
            }
            
            private fun visitParadoxConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                var operator = true
                var value = ""
                element.processChild p@{
                    val elementType = it.elementType
                    when(elementType) {
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
                val conditionalStack = ArrayDeque(fileConditionStack)
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, defaultValue, conditionalStack)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                //不需要继续向下遍历
            }
            
            override fun elementFinished(element: PsiElement?) {
                if(element is ParadoxScriptParameterCondition || element is ParadoxScriptInlineParameterCondition) {
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
        if(parameterInfos.isNullOrEmpty()) return true
        return parameterInfos.all f@{ parameterInfo ->
            //如果是条件参数，则为可选
            if(parameterInfo.conditionStack == null) return@f true
            //如果带有默认值，则为可选
            if(parameterInfo.defaultValue != null) return@f true
            //如果基于条件表达式上下文是可选的，则为可选
            if(parameterInfo.conditionStack.isNotEmpty()
                && parameterInfo.conditionStack.all { it.where { n -> parameterName == n || (argumentNames != null && argumentNames.contains(n)) } }) return@f true
            //如果作为传入参数的值，则认为是可选的
            if(parameterInfo.expressionConfigs.isNotEmpty()
                && parameterInfo.expressionConfigs.any { it is CwtValueConfig && it.propertyConfig?.expression?.type == CwtDataTypes.Parameter }) return@f true
            false
        }
    }
    
    fun completeParameters(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        //向上找到参数上下文
        val parameterContext = ParadoxParameterSupport.findContext(element) ?: return
        val parameterContextInfo = ParadoxParameterSupport.getContextInfo(parameterContext) ?: return
        if(parameterContextInfo.parameters.isEmpty()) return
        for((parameterName, parameterInfos) in parameterContextInfo.parameters) {
            ProgressManager.checkCanceled()
            val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
            //排除当前正在输入的那个
            if(parameterInfos.size == 1 && element isSamePosition parameter) continue
            val parameterElement = when {
                parameter is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(parameter)
                parameter is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(parameter)
                else -> null
            } ?: continue
            val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                .withIcon(PlsIcons.Nodes.Parameter)
                .withTypeText(parameterElement.contextName, parameterElement.contextIcon, true)
            result.addElement(lookupElement)
        }
    }
    
    fun completeArguments(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if(context.quoted) return //输入参数不允许用引号括起
        val from = ParadoxParameterContextReferenceInfo.From.Argument
        val config = context.config ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
        val namesToDistinct = mutableSetOf<String>().synced()
        //整合查找到的所有参数上下文
        val insertSeparator = context.isKey == true && context.contextElement !is ParadoxScriptPropertyKey
        ParadoxParameterSupport.processContext(element, contextReferenceInfo, true) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterSupport.getContextInfo(parameterContext) ?: return@p true
            if(parameterContextInfo.parameters.isEmpty()) return@p true
            for((parameterName, parameterInfos) in parameterContextInfo.parameters) {
                //排除已输入的
                if(parameterName in argumentNames) continue
                if(!namesToDistinct.add(parameterName)) continue
                
                val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
                val parameterElement = when {
                    parameter is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(parameter)
                    parameter is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(parameter)
                    else -> null
                } ?: continue
                val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                    .withIcon(PlsIcons.Nodes.Parameter)
                    .withTypeText(parameterElement.contextName, parameterElement.contextIcon, true)
                    .letIf(insertSeparator) {
                        it.withInsertHandler { c, _ ->
                            val editor = c.editor
                            val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
                            val text = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
                            EditorModificationUtil.insertStringAtCaret(editor, text, false, true)
                        }
                    }
                result.addElement(lookupElement)
            }
            true
        }
    }
    
    fun getReadWriteAccess(element: PsiElement): ReadWriteAccessDetector.Access {
        return when {
            element is ParadoxParameter -> ReadWriteAccessDetector.Access.Read
            element is ParadoxConditionParameter -> ReadWriteAccessDetector.Access.Read
            else -> ReadWriteAccessDetector.Access.Write
        }
    }
    
    fun getParameterElement(element: PsiElement): ParadoxParameterElement? {
        return when(element) {
            is ParadoxParameterElement -> element
            is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(element)
            is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(element)
            else -> null
        }
    }
    
    fun getParameterInfo(parameterElement: ParadoxParameterElement): ParadoxParameterInfo? {
        val rootFile = selectRootFile(parameterElement) ?: return null
        val project = parameterElement.project
        val configGroup = getConfigGroup(project, parameterElement.gameType)
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
        if(contextConfigs.isEmpty()) return null
        val configs = contextConfigs.singleOrNull()?.configs
        if(configs.isNullOrEmpty()) return null
        if(configs.any { it !is CwtValueConfig || it.isBlock }) return PlsBundle.message("complex")
        return configs.mapTo(mutableSetOf()) { it.expression.expressionString }.joinToString(" | ")
    }
    
    /**
     * 尝试推断得到参数对应的上下文CWT规则。
     */
    fun getInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        if(!getSettings().inference.parameterConfig) return emptyList()
        
        val parameterInfo = getParameterInfo(parameterElement) ?: return emptyList()
        return parameterInfo.getOrPutUserData(PlsKeys.parameterInferredContextConfigs) {
            ProgressManager.checkCanceled()
            withRecursionGuard("icu.windea.pls.lang.ParadoxParameterHandler.getInferredContextConfigs") {
                withCheckRecursion(parameterElement) {
                    doGetInferredContextConfigs(parameterElement)
                }
            } ?: emptyList()
        }
    }
    
    private fun doGetInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val fromConfig = doGetInferredContextConfigsFromConfig(parameterElement)
        if(fromConfig.isNotEmpty()) return fromConfig
        
        return doGetInferredContextConfigsFromUsages(parameterElement)
    }
    
    private fun doGetInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val configGroup = getConfigGroup(parameterElement.project, parameterElement.gameType)
        val configs = configGroup.parameters.getAllByTemplate(parameterElement.name, parameterElement, configGroup)
        val config = configs.findLast { it.contextKey == parameterElement.contextKey } ?: return emptyList()
        return config.getContextConfigs()
    }
    
    private fun doGetInferredContextConfigsFromUsages(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        ParadoxParameterSupport.processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = ParadoxParameterSupport.getContextInfo(context) ?: return@p true
            val contextConfigs = doGetInferredContextConfigsFromUsages(parameterElement.name, contextInfo).orNull()
            result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
        }
        return result.get().orEmpty()
    }
    
    private fun doGetInferredContextConfigsFromUsages(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>> {
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if(parameterInfos.isNullOrEmpty()) return emptyList()
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        parameterInfos.process { parameterInfo ->
            ProgressManager.checkCanceled()
            val contextConfigs = ParadoxParameterInferredConfigProvider.getContextConfigs(parameterInfo, parameterContextInfo).orNull()
            result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
        }
        return result.get().orEmpty()
    }
    
    fun isIgnoredInferredConfig(config: CwtValueConfig): Boolean {
        return when(config.expression.type) {
            CwtDataTypes.Any, CwtDataTypes.Other -> true
            else -> false
        }
    }
}

//rootFile -> cacheKey -> parameterInfo
//depends on config group
private val CwtConfigGroup.parameterInfoCache by createKeyDelegate(CwtConfigContext.Keys) {
    NestedCache<VirtualFile, _, _, _> { CacheBuilder.newBuilder().buildCache<String, ParadoxParameterInfo>().trackedBy { it.modificationTracker } }
}

private val PlsKeys.parameterInferredConfig by createKey<CwtValueConfig>("paradox.parameterInferredConfig")
private val PlsKeys.parameterInferredContextConfigs by createKey<List<CwtMemberConfig<*>>>("paradox.parameterInferredContextConfigs")