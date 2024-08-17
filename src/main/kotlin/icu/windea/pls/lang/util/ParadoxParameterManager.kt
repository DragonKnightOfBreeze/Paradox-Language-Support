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
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.model.path.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import java.util.*

object ParadoxParameterManager {
    object Keys: KeyRegistry() {
       val parameterInferredContextConfigs by createKey<List<CwtMemberConfig<*>>>(this)
       val parameterInferredContextConfigsFromConfig by createKey<List<CwtMemberConfig<*>>>(this)
    }
    
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
                val conditionalStack = ArrayDeque(fileConditionStack).orNull()
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
            if(parameterInfo.conditionStack.all { it.where { n -> parameterName == n || (argumentNames != null && argumentNames.contains(n)) } }) return@f true
            //如果作为传入参数的值，则认为是可选的
            if(parameterInfo.expressionConfigs.any { it is CwtValueConfig && it.propertyConfig?.expression?.type == CwtDataTypes.Parameter }) return@f true
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
            val lookupElement = ParadoxLookupElementBuilder.create(parameterElement, parameterName)
                .withIcon(PlsIcons.Nodes.Parameter)
                .withTypeText(parameterElement.contextName)
                .withTypeIcon(parameterElement.contextIcon)
                .build(context)
            result.addElement(lookupElement)
        }
        
        val contextKey = ParadoxParameterSupport.getContextKeyFromContext(parameterContext) ?: return
        context.contextKey = contextKey
        ParadoxCompletionManager.completeExtendedParameter(context, result)
    }
    
    fun completeArguments(element: PsiElement, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if(context.quoted) return //输入参数不允许用引号括起
        val from = ParadoxParameterContextReferenceInfo.From.Argument
        val config = context.config ?: return
        val completionOffset = context.parameters?.offset ?: return
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, config, completionOffset) ?: return
        val argumentNames = contextReferenceInfo.arguments.mapTo(mutableSetOf()) { it.argumentName }
        //整合查找到的所有参数上下文
        ParadoxParameterSupport.processContext(element, contextReferenceInfo, true) p@{ parameterContext ->
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterSupport.getContextInfo(parameterContext) ?: return@p true
            if(parameterContextInfo.parameters.isEmpty()) return@p true
            for((parameterName, parameterInfos) in parameterContextInfo.parameters) {
                //排除已输入的
                if(!argumentNames.add(parameterName)) continue
                
                val parameter = parameterInfos.firstNotNullOfOrNull { it.element } ?: continue
                val parameterElement = when {
                    parameter is ParadoxConditionParameter -> ParadoxParameterSupport.resolveConditionParameter(parameter)
                    parameter is ParadoxParameter -> ParadoxParameterSupport.resolveParameter(parameter)
                    else -> null
                } ?: continue
                val lookupElement = ParadoxLookupElementBuilder.create(parameterElement, parameterName)
                    .withIcon(PlsIcons.Nodes.Parameter)
                    .withTypeText(parameterElement.contextName)
                    .withTypeIcon(parameterElement.contextIcon)
                    .build(context)
                result.addElement(lookupElement)
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
        val parameterInfo = getParameterInfo(parameterElement) ?: return emptyList()
        return parameterInfo.getOrPutUserData(Keys.parameterInferredContextConfigs) {
            ProgressManager.checkCanceled()
            withRecursionGuard("icu.windea.pls.lang.ParadoxParameterManager.getInferredContextConfigs") {
                withCheckRecursion(parameterElement) {
                    doGetInferredContextConfigs(parameterElement)
                        .also { doOptimizeContextConfigsByLocation(parameterElement, it) }
                }
            } ?: emptyList()
        }
    }
    
    /**
     * 尝试（从扩展的CWT规则）推断得到参数对应的上下文CWT规则。
     */
    fun getInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val parameterInfo = getParameterInfo(parameterElement) ?: return emptyList()
        return parameterInfo.getOrPutUserData(Keys.parameterInferredContextConfigsFromConfig) {
            doGetInferredContextConfigsFromConfig(parameterElement)
                .also { doOptimizeContextConfigsByLocation(parameterElement, it) }
        }
    }
    
    private fun doGetInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val fromConfig = doGetInferredContextConfigsFromConfig(parameterElement)
        if(fromConfig.isNotEmpty()) return fromConfig
        
        if(!getSettings().inference.configContextForParameters) return emptyList()
        
        return doGetInferredContextConfigsFromUsages(parameterElement)
    }
    
    private fun doOptimizeContextConfigsByLocation(parameterElement: ParadoxParameterElement, contextConfigs: List<CwtMemberConfig<*>>) {
        val parent = parameterElement.parent?.parent
        contextConfigs.forEach f1@{
            val configs = it.configs
            if(configs.isNullOrEmpty()) return@f1
            if(configs !is MutableList) return@f1
            val keysToDistinct = mutableSetOf<String>()
            val opConfigs = mutableListOf<CwtMemberConfig<*>>()
            configs.forEach f2@{ config ->
                when(config) {
                    is CwtPropertyConfig -> {
                        if(parent is ParadoxScriptPropertyKey) {
                            if(config.isBlock) return@f2
                            if(!keysToDistinct.add(config.key)) return@f2
                            val opConfig = CwtValueConfig.resolve(emptyPointer(), config.configGroup, config.key)
                            opConfigs += opConfig
                        } else {
                            opConfigs += config
                        }
                    }
                    is CwtValueConfig -> {
                        if(!keysToDistinct.add(config.value)) return@f2
                        opConfigs += config
                    }
                }
            }
            configs.clear()
            configs += opConfigs
        }
    }
    
    private fun doGetInferredContextConfigsFromConfig(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val configGroup = getConfigGroup(parameterElement.project, parameterElement.gameType)
        val configs = configGroup.extendedParameters.findFromPattern(parameterElement.name, parameterElement, configGroup).orEmpty()
        val config = configs.findLast { it.contextKey.matchFromPattern(parameterElement.contextKey, parameterElement, configGroup) } ?: return emptyList()
        return config.getContextConfigs(parameterElement)
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
    
    /**
     * @param shift 从[element]开始向上的偏移，偏移量与[ParadoxElementPath]的长度的判定方式是一致的。
     */
    fun getParameterizedKeyConfigs(element: PsiElement, shift: Int): List<CwtValueConfig>? {
        val parameterizedProperty = element.parentsOfType<ParadoxScriptMemberElement>()
            .filter { it.isBlockMember() }
            .elementAtOrNull(shift)
            ?: return null
        val propertyKey = parameterizedProperty.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return null
        val parameter = propertyKey.findChild<ParadoxParameter>() ?: return null
        val parameterElement = getParameterElement(parameter) ?: return null
        val contextConfigs = getInferredContextConfigsFromConfig(parameterElement)
        val configs = contextConfigs.singleOrNull()?.configs
            ?.filterNot { it !is CwtValueConfig || it.isBlock }
        if(configs.isNullOrEmpty()) return null
        return configs.cast()
    }
}

//rootFile -> cacheKey -> parameterInfo
//depends on config group
private val CwtConfigGroup.parameterInfoCache by createKeyDelegate(CwtConfigContext.Keys) {
    createNestedCache<VirtualFile, _, _, _> {
        CacheBuilder.newBuilder().buildCache<String, ParadoxParameterInfo>().trackedBy { it.modificationTracker }
    }
}
