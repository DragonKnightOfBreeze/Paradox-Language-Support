package icu.windea.pls.lang.util

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

object ParadoxInlineScriptManager {
    object Keys : KeyRegistry() {
        val cachedInlineScriptUsageInfo by createKey<CachedValue<ParadoxInlineScriptUsageIndexInfo>>(this)
    }

    const val inlineScriptKey = "inline_script"
    const val inlineScriptExpressionOptionName = "inline_script_expression"
    const val inlineScriptPathExpressionString = "filepath[common/inline_scripts/,.txt]"

    val inlineScriptPathExpression = CwtDataExpression.resolve(inlineScriptPathExpressionString, false)

    fun getUsageInfo(element: ParadoxScriptProperty): ParadoxInlineScriptUsageIndexInfo? {
        val name = element.name.lowercase()
        if (name != inlineScriptKey) return null
        return doGetUsageInfoFromCache(element)
    }

    private fun doGetUsageInfoFromCache(element: ParadoxScriptProperty): ParadoxInlineScriptUsageIndexInfo? {
        //invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedInlineScriptUsageInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = runReadAction { doGetUsageInfo(element, file) }
            value.withDependencyItems(file)
        }
    }

    private fun doGetUsageInfo(element: ParadoxScriptProperty, file: PsiFile = element.containingFile): ParadoxInlineScriptUsageIndexInfo? {
        //这里不能调用ParadoxExpressionHandler.getConfigs，因为是需要处理内联的情况，可能会导致StackOverflow

        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val project = file.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val inlineConfigs = configGroup.inlineConfigGroup[inlineScriptKey] ?: return null
        val propertyValue = element.propertyValue ?: return null
        val matchOptions = Options.SkipIndex or Options.SkipScope
        val inlineConfig = inlineConfigs.find {
            val expression = ParadoxScriptExpression.resolve(propertyValue, matchOptions)
            ParadoxExpressionMatcher.matches(propertyValue, expression, it.config.valueExpression, it.config, configGroup).get(matchOptions)
        }
        if (inlineConfig == null) return null
        val expression = getInlineScriptExpressionFromInlineConfig(element, inlineConfig) ?: return null
        if (expression.isParameterized()) return null
        val elementOffset = element.startOffset
        return ParadoxInlineScriptUsageIndexInfo(expression, elementOffset, gameType)
    }

    fun getInlineScriptFile(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptFile? {
        val filePath = getInlineScriptFilePath(expression)
        val selector = selector(project, contextElement).file().contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)?.castOrNull()
    }

    fun processInlineScriptFile(expression: String, contextElement: PsiElement, project: Project, onlyMostRelevant: Boolean = false, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        val filePath = getInlineScriptFilePath(expression)
        val selector = selector(project, contextElement).file().contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).processQueryAsync(onlyMostRelevant) p@{
            ProgressManager.checkCanceled()
            val file = it.toPsiFile(project)?.castOrNull<ParadoxScriptFile>() ?: return@p true
            processor(file)
            true
        }
    }

    fun isInlineScriptExpressionConfig(config: CwtConfig<*>): Boolean {
        return config.configExpression == inlineScriptPathExpression
    }

    fun getInlineScriptFilePath(pathReference: String): String? {
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())?.firstOrNull()
    }

    fun getInlineScriptExpression(file: VirtualFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val filePath = fileInfo.path.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.orNull()
    }

    fun getInlineScriptExpression(file: PsiFile): String? {
        if (file !is ParadoxScriptFile) return null
        val vFile = selectFile(file) ?: return null
        return getInlineScriptExpression(vFile)
    }

    fun getInlineScriptExpressionFromInlineConfig(element: ParadoxScriptProperty, inlineConfig: CwtInlineConfig): String? {
        if (element.name.lowercase() != inlineScriptKey) return null
        if (inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement = element.findByPath(expressionLocation, ParadoxScriptValue::class.java)
        val expression = expressionElement?.stringValue() ?: return null
        return expression
    }

    fun getExpressionElement(contextReferenceElement: ParadoxScriptProperty): ParadoxScriptValue? {
        if (contextReferenceElement.name.lowercase() != inlineScriptKey) return null
        val config = ParadoxExpressionManager.getConfigs(contextReferenceElement).findIsInstance<CwtPropertyConfig>() ?: return null
        val inlineConfig = config.inlineConfig ?: return null
        if (inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement = contextReferenceElement.findByPath(expressionLocation, ParadoxScriptValue::class.java) ?: return null
        return expressionElement
    }

    fun getContextReferenceElement(expressionElement: PsiElement): ParadoxScriptProperty? {
        if (expressionElement !is ParadoxScriptValue) return null
        var contextReferenceElement = expressionElement.findParentProperty()?.castOrNull<ParadoxScriptProperty>() ?: return null
        if (contextReferenceElement.name.lowercase() != inlineScriptKey) {
            contextReferenceElement = contextReferenceElement.findParentProperty()?.castOrNull<ParadoxScriptProperty>() ?: return null
        }
        if (contextReferenceElement.name.lowercase() != inlineScriptKey) return null
        val config = ParadoxExpressionManager.getConfigs(contextReferenceElement).findIsInstance<CwtPropertyConfig>() ?: return null
        val inlineConfig = config.inlineConfig ?: return null
        if (inlineConfig.name != inlineScriptKey) return null
        val expressionLocation = inlineConfig.config.findOption { it.key == inlineScriptExpressionOptionName }?.stringValue ?: return null
        val expressionElement0 = contextReferenceElement.findByPath(expressionLocation, ParadoxScriptValue::class.java) ?: return null
        if (expressionElement0 != expressionElement) return null
        return contextReferenceElement
    }

    fun getInferredContextConfigs(contextElement: ParadoxScriptMemberElement, inlineScriptExpression: String, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        if (!PlsFacade.getSettings().inference.configContextForInlineScripts) return emptyList()
        return withRecursionGuard {
            withRecursionCheck(inlineScriptExpression) {
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
        val fromConfig = doGetInferredContextConfigsFromConfig(contextElement, context, inlineScriptExpression, matchOptions)
        if (fromConfig.isNotEmpty()) return fromConfig

        return doGetInferredContextConfigsFromUsages(contextElement, context, inlineScriptExpression, matchOptions)
    }

    private fun doGetInferredContextConfigsFromConfig(contextElement: ParadoxScriptMemberElement, context: CwtConfigContext, inlineScriptExpression: String, matchOptions: Int): List<CwtMemberConfig<*>> {
        val configGroup = context.configGroup
        val config = configGroup.extendedInlineScripts.findFromPattern(inlineScriptExpression, contextElement, configGroup, matchOptions) ?: return emptyList()
        return config.getContextConfigs()
    }

    private fun doGetInferredContextConfigsFromUsages(contextElement: ParadoxScriptMemberElement, context: CwtConfigContext, inlineScriptExpression: String, matchOptions: Int): List<CwtMemberConfig<*>> {
        // infer & merge
        val fastInference = PlsFacade.getSettings().inference.configContextForInlineScriptsFast
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        val project = context.configGroup.project
        val selector = selector(project, contextElement).inlineScriptUsage()
        ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            val file = info.virtualFile?.toPsiFile(project) ?: return@p true
            val elementOffsets = info.elementOffsets.orNull() ?: return@p true
            elementOffsets.process p1@{ elementOffset ->
                val e = file.findElementAt(elementOffset) ?: return@p1 true
                val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p1 true
                if (!p.name.equals(inlineScriptKey, true)) return@p1 true
                val memberElement = p.parentOfType<ParadoxScriptMemberElement>() ?: return@p1 true
                val usageConfigContext = ParadoxExpressionManager.getConfigContext(memberElement) ?: return@p1 true
                val usageConfigs = usageConfigContext.getConfigs(matchOptions).orNull()
                if(fastInference && usageConfigs.isNotNullOrEmpty()) {
                    result.set(usageConfigs)
                    return@p1 false
                }
                // merge
                result.mergeValue(usageConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }.also {
                    if (it) return@also
                    context.inlineScriptHasConflict = true
                    result.set(null)
                }
            }
        }
        return result.get().orEmpty()
    }
}
