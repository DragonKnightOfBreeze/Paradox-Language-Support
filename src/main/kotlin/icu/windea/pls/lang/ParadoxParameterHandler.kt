package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.highlighting.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.lang.parameter.*
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
        val fileConditionStack = LinkedList<ReversibleValue<String>>()
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxParameter) visitParameter(element)
                if(element is ParadoxScriptParameterConditionExpression) visitParadoxConditionExpression(element)
                super.visitElement(element)
            }
            
            private fun visitParadoxConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                ProgressManager.checkCanceled()
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
            
            private fun visitParameter(element: ParadoxParameter) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalStack = if(fileConditionStack.isEmpty()) null else LinkedList(fileConditionStack)
                val info = ParadoxParameterContextInfo.Parameter(element.createPointer(file), name, defaultValue, conditionalStack)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                //不需要继续向下遍历
            }
            
            override fun elementFinished(element: PsiElement?) {
                if(element is ParadoxScriptParameterCondition) finishParadoxCondition()
            }
            
            private fun finishParadoxCondition() {
                fileConditionStack.removeLast()
            }
        })
        return ParadoxParameterContextInfo(parameters, file.project, gameType)
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
            val parameterElement = ParadoxParameterSupport.resolveParameter(parameter)
                ?: continue
            val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                .withIcon(PlsIcons.Parameter)
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
                val parameterElement = ParadoxParameterSupport.resolveParameter(parameter)
                    ?: continue
                val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
                    .withIcon(PlsIcons.Parameter)
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
    
    /**
     * 尝试推断得到参数对应的CWT规则。
     */
    fun getInferredConfig(parameterElement: ParadoxParameterElement): CwtValueConfig? {
        val cacheKey = parameterElement.contextKey + "@" + parameterElement.name
        val parameterCache = selectRootFile(parameterElement.parent)?.getUserData(Keys.parameterCache) ?: return null
        val cached = parameterCache.get(cacheKey)
        if(cached != null) {
            val modificationTracker = cached.getUserData(Keys.parameterModificationTracker)
            if(modificationTracker != null) {
                val modificationCount = cached.getUserData(Keys.parameterModificationCount) ?: 0
                if(modificationCount == modificationTracker.modificationCount) {
                    val resolved = cached.getUserData(Keys.inferredConfig)
                    if(resolved != null) {
                        return resolved.takeIf { it !== CwtValueConfig.EmptyConfig }
                    }
                }
            }
        }
        
        val result = Ref.create<CwtValueConfig>()
        ParadoxParameterSupport.processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = ParadoxParameterSupport.getContextInfo(context) ?: return@p true
            val config = getInferredConfig(parameterElement.name, contextInfo)
            result.mergeValue(config) { v1, v2 -> ParadoxConfigMergeHandler.mergeValueConfig(v1, v2) }
        }
        val resolved = result.get()
        
        val ep = parameterElement.getUserData(ParadoxParameterSupport.Keys.support)
        if(ep != null) {
            val modificationTracker = ep.getModificationTracker(parameterElement)
            if(modificationTracker != null) {
                parameterElement.putUserData(Keys.inferredConfig, resolved ?: CwtValueConfig.EmptyConfig)
                parameterElement.putUserData(Keys.parameterModificationTracker, modificationTracker)
                parameterElement.putUserData(Keys.parameterModificationCount, modificationTracker.modificationCount)
                parameterCache.put(cacheKey, parameterElement)
            }
        }
        
        return resolved
    }
    
    fun getInferredConfig(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        //如果推断得到的规则不唯一，则返回null
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if(parameterInfos.isNullOrEmpty()) return null
        val result = Ref.create<CwtValueConfig>()
        parameterInfos.process { parameterInfo ->
            ProgressManager.checkCanceled()
            val config = ParadoxParameterInferredConfigProvider.getConfig(parameterInfo, parameterContextInfo)
            result.mergeValue(config) { v1, v2 -> ParadoxConfigMergeHandler.mergeValueConfig(v1, v2) }
        }
        return result.get()
    }
    
    /**
     * 尝试推断得到参数对应的上下文CWT规则。
     */
    fun getInferredContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        val cacheKey = parameterElement.contextKey + "@" + parameterElement.name
        val parameterCache = selectRootFile(parameterElement.parent)?.getUserData(Keys.parameterCache) ?: return emptyList()
        val cached = parameterCache.get(cacheKey)
        if(cached != null) {
            val modificationTracker = cached.getUserData(Keys.parameterModificationTracker)
            if(modificationTracker != null) {
                val modificationCount = cached.getUserData(Keys.parameterModificationCount) ?: 0
                if(modificationCount == modificationTracker.modificationCount) {
                    val resolved = cached.getUserData(Keys.inferredContextConfigs)
                    if(resolved != null) {
                        return resolved
                    }
                }
            }
        }
        
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        ParadoxParameterSupport.processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = ParadoxParameterSupport.getContextInfo(context) ?: return@p true
            val contextConfigs = getInferredContextConfigs(parameterElement.name, contextInfo).takeIfNotEmpty()
            result.mergeValue(contextConfigs) { v1, v2 -> ParadoxConfigMergeHandler.mergeConfigs(v1, v2) }
        }
        val resolved = result.get().orEmpty()
        
        val ep = parameterElement.getUserData(ParadoxParameterSupport.Keys.support)
        if(ep != null) {
            val modificationTracker = ep.getModificationTracker(parameterElement)
            if(modificationTracker != null) {
                parameterElement.putUserData(Keys.inferredContextConfigs, resolved)
                parameterElement.putUserData(Keys.parameterModificationTracker, modificationTracker)
                parameterElement.putUserData(Keys.parameterModificationCount, modificationTracker.modificationCount)
                parameterCache.put(cacheKey, parameterElement)
            }
        }
        
        return resolved
    }
    
    fun getInferredContextConfigs(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>> {
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if(parameterInfos.isNullOrEmpty()) return emptyList()
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        parameterInfos.process { parameterInfo ->
            ProgressManager.checkCanceled()
            val contextConfigs = ParadoxParameterInferredConfigProvider.getContextConfigs(parameterInfo, parameterContextInfo).takeIfNotEmpty()
            result.mergeValue(contextConfigs) { v1,v2 -> ParadoxConfigMergeHandler.mergeConfigs(v1, v2) }
        }
        return result.get().orEmpty()
    }
    
    fun isIgnoredInferredConfig(config: CwtValueConfig): Boolean {
        return when(config.expression.type) {
            CwtDataType.Any, CwtDataType.Other -> true
            else -> false
        }
    }
    
    object Keys {
        val inferredConfig = Key.create<CwtValueConfig>("paradox.parameter.inferredConfig")
        val inferredContextConfigs = Key.create<List<CwtMemberConfig<*>>>("paradox.parameter.inferredContextConfigs")
        val parameterCache = KeyWithDefaultValue.create<Cache<String, ParadoxParameterElement>>("paradox.parameter.cache") { CacheBuilder.newBuilder().buildCache() }
        val parameterModificationTracker = Key.create<ModificationTracker>("paradox.parameter.modificationTracker")
        val parameterModificationCount = Key.create<Long>("paradox.parameter.modificationCount")
    }
}