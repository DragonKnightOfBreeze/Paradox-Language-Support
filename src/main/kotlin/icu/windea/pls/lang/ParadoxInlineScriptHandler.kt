package icu.windea.pls.lang

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*

object ParadoxInlineScriptHandler {
    const val inlineScriptKey = "inline_script"
    const val inlineScriptExpressionOptionName = "inline_script_expression"
    const val inlineScriptPathExpressionString = "filepath[common/inline_scripts/,.txt]"
    
    val inlineScriptPathExpression = CwtValueExpression.resolve(inlineScriptPathExpressionString)
    
    val cachedInlineScriptUsageInfoKey = createKey<CachedValue<ParadoxInlineScriptUsageInfo>>("paradox.cached.inlineScriptUsageInfo")
    
    fun getUsageInfo(element: ParadoxScriptProperty): ParadoxInlineScriptUsageInfo? {
        val name = element.name.lowercase()
        if(name != inlineScriptKey) return null
        return doGetUsageInfoFromCache(element)
    }
    
    private fun doGetUsageInfoFromCache(element: ParadoxScriptProperty): ParadoxInlineScriptUsageInfo? {
        return CachedValuesManager.getCachedValue(element, cachedInlineScriptUsageInfoKey) {
            ProgressManager.checkCanceled()
            val file = element.containingFile ?: return@getCachedValue null
            val value = runReadAction { doGetUsageInfo(element, file) }
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetUsageInfo(element: ParadoxScriptProperty, file: PsiFile = element.containingFile): ParadoxInlineScriptUsageInfo? {
        //这里不能调用CwtConfigHandler.getConfigs，因为需要处理内联的情况，会导致StackOverflow
        
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val project = file.project
        val configGroup = getConfigGroup(project, gameType)
        val inlineConfigs = configGroup.inlineConfigGroup[inlineScriptKey] ?: return null
        val propertyValue = element.propertyValue ?: return null
        val matchOptions = Options.SkipIndex or Options.SkipScope
        val inlineConfig = inlineConfigs.find {
            val expression = ParadoxDataExpression.resolve(propertyValue, matchOptions)
            CwtConfigMatcher.matches(propertyValue, expression, it.config.valueExpression, it.config, configGroup).get(matchOptions)
        }
        if(inlineConfig == null) return null
        val expression = getInlineScriptExpressionFromInlineConfig(element, inlineConfig) ?: return null
        if(expression.isParameterized()) return null
        val elementOffset = element.startOffset
        return ParadoxInlineScriptUsageInfo(expression, elementOffset, gameType)
    }
    
    fun getInlineScriptFile(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptFile? {
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)?.castOrNull()
    }
    
    fun processInlineScriptFile(expression: String, contextElement: PsiElement, project: Project, onlyMostRelevant: Boolean = false, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        val filePath = getInlineScriptFilePath(expression)
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).processQueryAsync(onlyMostRelevant) p@{
            ProgressManager.checkCanceled()
            val file = it.toPsiFile(project)?.castOrNull<ParadoxScriptFile>() ?: return@p true
            processor(file)
            true
        }
    }
    
    fun getInlineScriptFilePath(pathReference: String): String? {
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())?.firstOrNull()
    }
    
    fun getInlineScriptExpression(file: VirtualFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val filePath = fileInfo.pathToEntry.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.orNull()
    }
    
    fun getInlineScriptExpression(file: PsiFile): String? {
        if(file !is ParadoxScriptFile) return null
        val vFile = selectFile(file) ?: return null
        return getInlineScriptExpression(vFile)
    }
    
    fun getInlineScriptExpressionFromInlineConfig(element: ParadoxScriptProperty, inlineConfig: CwtInlineConfig): String? {
        if(element.name.lowercase() != inlineScriptKey) return null
        if(inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement = element.findByPath(expressionLocation, ParadoxScriptValue::class.java)
        val expression = expressionElement?.stringValue() ?: return null
        return expression
    }
    
    fun getExpressionElement(contextReferenceElement: ParadoxScriptProperty):  ParadoxScriptValue? {
        if(contextReferenceElement.name.lowercase() != inlineScriptKey) return null
        val config = CwtConfigHandler.getConfigs(contextReferenceElement).firstOrNull() ?: return null
        val inlineConfig = config.inlineableConfig?.castOrNull<CwtInlineConfig>() ?: return null
        if(inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement = contextReferenceElement.findByPath(expressionLocation, ParadoxScriptValue::class.java) ?: return null
        return expressionElement
    }
    
    fun getContextReferenceElement(expressionElement: PsiElement): ParadoxScriptProperty? {
        if(expressionElement !is ParadoxScriptValue) return null
        var contextReferenceElement = expressionElement.findParentProperty()?.castOrNull<ParadoxScriptProperty>() ?: return null
        if(contextReferenceElement.name.lowercase() != inlineScriptKey) {
            contextReferenceElement = contextReferenceElement.findParentProperty()?.castOrNull<ParadoxScriptProperty>() ?: return null
        }
        if(contextReferenceElement.name.lowercase() != inlineScriptKey) return null
        val config = CwtConfigHandler.getConfigs(contextReferenceElement).firstOrNull() ?: return null
        val inlineConfig = config.inlineableConfig?.castOrNull<CwtInlineConfig>() ?: return null
        if(inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement0 = contextReferenceElement.findByPath(expressionLocation, ParadoxScriptValue::class.java) ?: return null
        if(expressionElement0 != expressionElement) return null
        return contextReferenceElement
    }
    
    fun getInferredContextConfigs(contextElement: ParadoxScriptMemberElement, inlineScriptExpression: String, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        if(!getSettings().inference.inlineScriptConfig) return emptyList()
        
        return withRecursionGuard("icu.windea.pls.lang.ParadoxInlineScriptHandler.getInferredContextConfigs") {
            withCheckRecursion(inlineScriptExpression) {
                context.inlineScriptHasConflict = false
                context.inlineScriptHasRecursion = false
                doGetInferredContextConfigs(contextElement, context, inlineScriptExpression, matchOptions)
            }
        } ?: run {
            context.inlineScriptHasRecursion = true
            emptyList()
        }
    }
    
    private fun doGetInferredContextConfigs(contextElement: ParadoxScriptMemberElement, context: CwtConfigContext, inlineScriptExpression: String, matchOptions: Int): List<CwtMemberConfig<*>> {
        return doGetInferredContextConfigsFromUsages(contextElement, context, inlineScriptExpression, matchOptions)
    }
    
    private fun doGetInferredContextConfigsFromUsages(contextElement: ParadoxScriptMemberElement, context: CwtConfigContext, inlineScriptExpression: String, matchOptions: Int): List<CwtMemberConfig<*>> {
        // infer & merge
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        val project = context.configGroup.project
        val selector = inlineScriptSelector(project, contextElement)
        ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            val file = info.virtualFile?.toPsiFile(project) ?: return@p true
            val e = file.findElementAt(info.elementOffset) ?: return@p true
            val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p true
            if(!p.name.equals(inlineScriptKey, true)) return@p true
            val memberElement = p.parentOfType<ParadoxScriptMemberElement>() ?: return@p true
            val usageConfigContext = CwtConfigHandler.getConfigContext(memberElement) ?: return@p true
            val usageConfigs = usageConfigContext.getConfigs(matchOptions).orNull()
            // merge
            result.mergeValue(usageConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }.also {
                if(it) return@also
                context.inlineScriptHasConflict = true
                result.set(null)
            }
        }
        return result.get().orEmpty()
    }
}