package icu.windea.pls.lang

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxInlineScriptHandler {
    const val inlineScriptKey = "inline_script"
    
    val inlineScriptPathExpression = CwtValueExpression.resolve("filepath[common/inline_scripts/,.txt]")
    
    val cachedInlineScriptUsageInfoKey = Key.create<CachedValue<ParadoxInlineScriptUsageInfo>>("paradox.cached.inlineScriptUsageInfo")
    
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
        //这里不能调用ParadoxConfigHandler.getConfigs，因为需要处理内联的情况，会导致StackOverflow
        
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val project = file.project
        val configGroup = getCwtConfig(project).get(gameType)
        val inlineConfigs = configGroup.inlineConfigGroup[inlineScriptKey] ?: return null
        val propertyValue = element.propertyValue ?: return null
        val matchOptions = Options.SkipIndex or Options.SkipScope or Options.Fast
        val inlineConfig = ParadoxConfigMatcher.find(inlineConfigs, matchOptions) {
            val expression = ParadoxDataExpression.resolve(propertyValue, matchOptions)
            ParadoxConfigMatcher.matches(propertyValue, expression, it.config.valueExpression, it.config, configGroup)
        }
        if(inlineConfig == null) return null
        val expression = getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterized()) return null
        val elementOffset = element.startOffset
        return ParadoxInlineScriptUsageInfo(expression, elementOffset, gameType)
    }
    
    private fun getExpressionLocation(it: CwtMemberConfig<*>): String? {
        return it.findOption { it.key == "inline_script_expression" }?.stringValue
    }
    
    fun getExpressionFromInlineConfig(propertyValue: ParadoxScriptValue, inlineConfig: CwtInlineConfig): String? {
        if(inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = getExpressionLocation(inlineConfig.config) ?: return null
        val expressionElement = if(expressionLocation.isEmpty()) {
            propertyValue.castOrNull<ParadoxScriptString>()
        } else {
            propertyValue.findProperty(expressionLocation, conditional = true)?.propertyValue?.castOrNull<ParadoxScriptString>()
        }
        return expressionElement?.stringValue()
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
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())
    }
    
    fun getInlineScriptExpression(file: VirtualFile): String? {
        val fileInfo = file.fileInfo ?: return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    fun getInlineScriptExpression(file: PsiFile): String? {
        if(file !is ParadoxScriptFile) return null
        val fileInfo = file.fileInfo ?: return null
        return doGetInlineScriptExpression(fileInfo)
    }
    
    private fun doGetInlineScriptExpression(fileInfo: ParadoxFileInfo): String? {
        val filePath = fileInfo.pathToEntry.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.takeIfNotEmpty()
    }
}