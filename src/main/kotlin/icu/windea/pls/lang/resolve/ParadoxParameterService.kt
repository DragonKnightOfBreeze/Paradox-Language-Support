package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.collections.findLastFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.constants.StatusStrings
import icu.windea.pls.core.mergeValue
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterInferredConfigProvider
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.model.support
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptParameter

@Optimized
object ParadoxParameterService {
    /**
     * @see ParadoxParameterSupport.isContext
     */
    @Suppress("unused")
    fun isContext(element: ParadoxDefinitionElement): Boolean {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        return supports.anyFast { support ->
            support.isContext(element)
        }
    }

    /**
     * @see ParadoxParameterSupport.findContext
     */
    fun findContext(element: PsiElement): ParadoxDefinitionElement? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.findContext(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.resolveParameter
     */
    fun resolveParameter(element: ParadoxScriptParameter): ParadoxParameterLightElement? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveParameter(element)?.also { it.support = support }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.resolveConditionParameter
     */
    fun resolveConditionParameter(element: ParadoxScriptConditionParameter): ParadoxParameterLightElement? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveConditionParameter(element)?.also { it.support = support }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.resolveArgument
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.resolveArgument(element, rangeInExpression, config)?.also { it.support = support }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.processContext
     */
    fun processContext(element: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        return supports.processFast { support ->
            support.processContext(element, onlyMostRelevant, processor)
        }
    }

    /**
     * @see ParadoxParameterSupport.processContextReference
     */
    fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        return supports.processFast { support ->
            support.processContextReference(element, contextReferenceInfo, onlyMostRelevant, processor)
        }
    }

    fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.getContextInfo(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.getContextReferenceInfo
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.getContextReferenceInfo(element, from, *extraArgs)?.also { it.support = support }?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterSupport.getContextKeyFromContext
     */
    fun getContextKeyFromContext(element: ParadoxDefinitionElement): String? {
        val supports = ParadoxParameterSupport.EP_NAME.extensionList
        supports.forEachFast { support ->
            support.getContextKeyFromContext(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxParameterInferredConfigProvider.getContextConfigs
     */
    fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val gameType = parameterContextInfo.gameType
        val eps = ParadoxParameterInferredConfigProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(parameterInfo, parameterContextInfo)) return@f
            ep.getContextConfigs(parameterInfo, parameterContextInfo).orNull()?.let { return it }
        }
        return null
    }

    fun getInferredConfigsForLiteral(contextConfigs: List<CwtMemberConfig<*>>): List<CwtValueConfig> {
        val configs = contextConfigs.singleOrNull()?.configs
            ?.filterNot { it !is CwtValueConfig || it.valueType == CwtExpressionType.Block }
        if (configs.isNullOrEmpty()) return emptyList()
        return configs.cast()
    }

    fun getInferredType(contextConfigs: List<CwtMemberConfig<*>>): String? {
        val configs = contextConfigs.singleOrNull()?.configs
        if (configs.isNullOrEmpty()) return null
        if (configs.anyFast { it !is CwtValueConfig || it.valueType == CwtExpressionType.Block }) return StatusStrings.complex
        return configs.mapFast { it.configExpression.expressionString }.toSet().joinToString(" | ")
    }

    fun getInferredContextConfigsFromConfig(parameterElement: ParadoxParameterLightElement, fast: Boolean = true): List<CwtMemberConfig<*>> {
        return doGetInferredContextConfigsFromConfig(parameterElement, fast)
    }

    private fun doGetInferredContextConfigsFromConfig(parameterElement: ParadoxParameterLightElement, fast: Boolean): List<CwtMemberConfig<*>> {
        val configGroup = ChronicleFacade.getConfigGroup(parameterElement.project, parameterElement.gameType)
        val configs = configGroup.extendedParameters.findByPattern(parameterElement.name, parameterElement, configGroup)
        if (configs.isNullOrEmpty()) return emptyList()
        val config = when {
            fast -> configs.findFast { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) }
            else -> configs.findLastFast { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) }
        }
        if (config == null) return emptyList()
        return ParadoxExtendedConfigService.getContextConfigs(config, parameterElement)
    }

    fun getInferredContextConfigsFromUsages(parameterElement: ParadoxParameterLightElement, fast: Boolean = true): List<CwtMemberConfig<*>> {
        withRecursionGuard({}.javaClass.name) {
            withRecursionCheck(parameterElement) {
                return doGetInferredContextConfigsFromUsages(parameterElement, fast)
            }
        }
        return emptyList()
    }

    private fun doGetInferredContextConfigsFromUsages(parameterElement: ParadoxParameterLightElement, fast: Boolean): List<CwtMemberConfig<*>> {
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = ParadoxParameterManager.getContextInfo(context) ?: return@p true
            val contextConfigs = doGetInferredContextConfigsFromUsages(parameterElement.name, contextInfo, fast).orNull()
            // merge
            val r = result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulationService.mergeConfigs(v1, v2) }
            if (fast && isFastInferenceAvailable(result)) false else r
        }
        return result.get().orEmpty()
    }

    private fun doGetInferredContextConfigsFromUsages(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo, fast: Boolean): List<CwtMemberConfig<*>> {
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if (parameterInfos.isNullOrEmpty()) return emptyList()
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        parameterInfos.process p@{ parameterInfo ->
            ProgressManager.checkCanceled()
            val contextConfigs = getContextConfigs(parameterInfo, parameterContextInfo).orNull()
            // merge
            val r = result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulationService.mergeConfigs(v1, v2) }
            if (fast && isFastInferenceAvailable(result)) false else r
        }
        return result.get().orEmpty()
    }

    private fun isFastInferenceAvailable(result: Ref<List<CwtMemberConfig<*>>>): Boolean {
        val v = result.get()
        if (v.isNullOrEmpty()) return false // empty -> not available
        val c = v.singleOrNull()?.configs?.singleOrNull()
        if (c is CwtValueConfig && c.configExpression.metadata.wildcard) return false // wildcard (e.g., from condition parameter) -> not available
        return true
    }
}
