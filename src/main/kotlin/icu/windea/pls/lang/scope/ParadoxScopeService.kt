package icu.windea.pls.lang.scope

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberType
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.config.resolvedOrNull
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.findIsInstanceFast
import icu.windea.pls.core.collections.findLastFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.scope.ParadoxDefinitionInferredScopeContextProvider
import icu.windea.pls.ep.scope.ParadoxDefinitionScopeContextProvider
import icu.windea.pls.ep.scope.ParadoxDefinitionSupportedScopesProvider
import icu.windea.pls.ep.scope.ParadoxDynamicValueInferredScopeContextProvider
import icu.windea.pls.ep.scope.ParadoxDynamicValueScopeContextProvider
import icu.windea.pls.ep.scope.ParadoxOverriddenScopeContextProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.ParadoxExtendedConfigService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScopeManager.findParentMember
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.isExact
import icu.windea.pls.model.scope.overriddenProvider
import icu.windea.pls.model.scope.promotions
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

@Optimized
object ParadoxScopeService {
    /**
     * @see ParadoxDefinitionSupportedScopesProvider.getSupportedScopes
     */
    fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionSupportedScopesProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(definition, definitionInfo)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            ep.getSupportedScopes(definition, definitionInfo)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDefinitionScopeContextProvider.getScopeContext
     */
    fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(definition, definitionInfo)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            ep.getScopeContext(definition, definitionInfo)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        val configGroup = definitionInfo.configGroup
        var map: Map<String, String>? = null
        val eps = ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(definition, definitionInfo)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeMergeService.mergeScopeContextMap(map, info.scopeContextMap, configGroup)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.resolve(resultMap)
        return result
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getMessage
     */
    @Suppress("unused")
    fun getInferenceMessage(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        var message: String? = null
        val eps = ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (info.hasConflict) return@f
            if (message == null) {
                message = ep.getMessage(definition, definitionInfo, info)
            } else {
                return ParadoxDefinitionInferredScopeContextProvider.getDefaultMessage(definition, definitionInfo, info)
            }
        }
        return message
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getErrorMessage
     */
    fun getInferenceErrorMessage(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        var errorMessage: String? = null
        val eps = ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (!info.hasConflict) return@f
            if (errorMessage == null) {
                errorMessage = ep.getErrorMessage(definition, definitionInfo, info)
            } else {
                return ParadoxDefinitionInferredScopeContextProvider.getDefaultErrorMessage(definition, definitionInfo, info)
            }
        }
        return errorMessage
    }

    /**
     * @see ParadoxDynamicValueScopeContextProvider.getScopeContext
     */
    fun getScopeContext(element: ParadoxDynamicValueLightElement): ParadoxScopeContext? {
        val gameType = element.gameType
        val eps = ParadoxDynamicValueScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(element)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            ep.getScopeContext(element)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDynamicValueInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(dynamicValue: ParadoxDynamicValueLightElement): ParadoxScopeContext? {
        val gameType = dynamicValue.gameType
        val configGroup = ChronicleFacade.getConfigGroup(dynamicValue.project, gameType)
        var map: Map<String, String>? = null
        val eps = ParadoxDynamicValueInferredScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            if (!ep.supports(dynamicValue)) return@f
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            val info = ep.getScopeContext(dynamicValue) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeMergeService.mergeScopeContextMap(map, info.scopeContextMap, configGroup)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.resolve(resultMap)
        return result
    }

    /**
     * @see ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext
     */
    fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        val gameType = config.configGroup.gameType
        val eps = ParadoxOverriddenScopeContextProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            ep.getOverriddenScopeContext(contextElement, config, parentScopeContext)?.also { it.overriddenProvider = ep }?.let { return it }
        }
        return null
    }

    fun isScopeContextSupportedForMember(element: ParadoxScriptMember, indirect: Boolean = false): Boolean {
        // some definitions, such as `on_action`, do support scope context on definition level
        if (isScopeContextSupportedForDefinition(element, indirect)) return true
        // if matched configs are scope-aware, so do supported
        if (isScopeContextSupportedForDefinitionMember(element)) return true
        // if there is an overridden scope context, so do supported
        val scopeContext = ParadoxScopeManager.getScopeContext(element)
        if (scopeContext?.overriddenProvider != null) return true
        return false
    }

    private fun isScopeContextSupportedForDefinition(element: ParadoxScriptMember, indirect: Boolean = false): Boolean {
        // should be a definition
        if (element !is ParadoxDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false

        val configGroup = definitionInfo.configGroup
        val definitionType = definitionInfo.type
        if (definitionType in configGroup.typeModel.supportScope) return true
        if (indirect && definitionType in configGroup.typeModel.supportIndirectScope) return true
        return false
    }

    private fun isScopeContextSupportedForDefinitionMember(element: ParadoxScriptMember): Boolean {
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        if (configs.isEmpty()) return false
        return configs.anyFast { isScopeContextSupportedFromConfig(it) }
    }

    private fun isScopeContextSupportedFromConfig(config: CwtMemberConfig<*>): Boolean {
        if (config.configExpression.type == CwtDataTypes.AliasKeysField) return true
        // from root
        if (isScopeContextSupportedFromRootConfig(config)) return true
        // from child
        if (isScopeContextSupportedFromChildConfig(config)) return true
        return false
    }

    private fun isScopeContextSupportedFromRootConfig(config: CwtMemberConfig<*>): Boolean {
        val properties = config.properties ?: return false
        val configGroup = config.configGroup
        properties.forEachFast f@{ property ->
            val aliasName = when {
                property.keyExpression.type == CwtDataTypes.AliasName -> property.keyExpression.metadata.value
                else -> return@f
            }
            if (aliasName in configGroup.aliasNamesSupportScope) return true
        }
        return false
    }

    private fun isScopeContextSupportedFromChildConfig(config: CwtMemberConfig<*>): Boolean {
        val configGroup = config.configGroup
        var currentConfig = config
        while (true) {
            if (currentConfig is CwtPropertyConfig) {
                val aliasConfig = currentConfig.aliasConfig
                if (aliasConfig != null) {
                    val aliasName = aliasConfig.name
                    if (aliasName in configGroup.aliasNamesSupportScope) return true
                }
            } else if (currentConfig is CwtValueConfig) {
                currentConfig = currentConfig.propertyConfig ?: currentConfig
            }
            currentConfig = currentConfig.parentConfig ?: break
        }
        return false
    }

    fun evaluateScopeContextForMember(element: ParadoxScriptMember): ParadoxScopeContext? {
        evaluateScopeContextForDefinition(element)?.let { return it }
        evaluateScopeContextForDefinitionMember(element)?.let { return it }
        return null
    }

    private fun evaluateScopeContextForDefinition(element: ParadoxScriptMember): ParadoxScopeContext? {
        // should be a definition
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null

        // get provided scope context from EPs, and use it if not exact
        val scopeContext = getScopeContext(element, definitionInfo)
        if (scopeContext != null && scopeContext.isExact) return scopeContext

        // get inferred scope context from EPs, and use the merged result if exists
        val inferredScopeContext = getInferredScopeContext(element, definitionInfo)
        if (inferredScopeContext != null) {
            val configGroup = definitionInfo.configGroup
            val mergedScopeContext = ParadoxScopeMergeService.mergeScopeContext(scopeContext, inferredScopeContext, configGroup)
            return mergedScopeContext ?: ParadoxScopeContext.resolveAny()
        }

        return scopeContext ?: ParadoxScopeContext.resolveAny()
    }

    private fun evaluateScopeContextForDefinitionMember(element: ParadoxScriptMember): ParadoxScopeContext? {
        // element could be a definition member only if after inlined
        val parentMember = findParentMember(element, withSelf = false)
        val parentScopeContext = if (parentMember != null) ParadoxScopeManager.getScopeContext(parentMember) else null
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        val config = configs.firstOrNull() ?: return null

        val overriddenScopeContext = getOverriddenScopeContext(element, config, parentScopeContext)
        if (overriddenScopeContext != null) return overriddenScopeContext

        if (config.memberType == CwtMemberType.PROPERTY && config.configExpression.type == CwtDataTypes.ScopeField) {
            if (parentScopeContext == null) return null
            val expressionElement = element.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return null
            val expressionString = expressionElement.value
            val configGroup = config.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, null, configGroup) ?: return null
            val result = ParadoxScopeManager.getScopeContext(expressionElement, scopeFieldExpression, parentScopeContext)
            return result
        } else {
            // 优先基于内联前的规则，如果没有，再基于内联后的规则
            val replaceScopes = config.optionMetadata.replaceScopes ?: config.resolvedOrNull()?.optionMetadata?.replaceScopes
            val pushScope = config.optionMetadata.pushScope ?: config.resolved().optionMetadata.pushScope
            val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) } ?: parentScopeContext ?: return null
            val result = scopeContext.resolveNext(pushScope)
            return result
        }
    }

    fun evaluateScopeContextForDynamicValue(element: ParadoxDynamicValueLightElement): ParadoxScopeContext {
        // get provided scope context from EPs, and use it if not exact
        val scopeContext = getScopeContext(element)
        if (scopeContext != null && scopeContext.isExact) return scopeContext

        // get inferred scope context from EPs, and use the merged result if exists
        val inferredScopeContext = getInferredScopeContext(element)
        if (inferredScopeContext != null) {
            val configGroup = ChronicleFacade.getConfigGroup(element.project, element.gameType)
            val mergedScopeContext = ParadoxScopeMergeService.mergeScopeContext(scopeContext, inferredScopeContext, configGroup)
            return mergedScopeContext ?: ParadoxScopeContext.resolveAny()
        }

        return scopeContext ?: ParadoxScopeContext.resolveAny()
    }

    fun evaluateScopeContextForExpression(element: ParadoxScriptMember, expression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxScopeContext? {
        val parentElement = findParentMember(element, withSelf = false)
        val parentScopeContext = when {
            parentElement != null -> ParadoxScopeManager.getScopeContext(parentElement) ?: ParadoxScopeContext.resolveAny()
            else -> ParadoxScopeContext.resolveAny()
        }
        val expressionElement = when {
            element is ParadoxScriptProperty -> if (configExpression.role.isKey()) element.propertyKey else element.propertyValue
            element is ParadoxScriptValue -> element
            else -> null
        }
        if (expressionElement == null) return null
        return ParadoxScopeManager.getScopeContext(expressionElement, expression, parentScopeContext)
    }

    fun evaluateScopeContextForExpression(element: ParadoxExpressionElement, expression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val scopeNodes = expression.scopeNodes
        if (scopeNodes.isEmpty()) return inputScopeContext // unexpected -> unchanged
        var result = inputScopeContext
        val links = mutableListOf<Tuple2<ParadoxScopeNode, ParadoxScopeContext>>()
        run {
            scopeNodes.forEachFast { scopeNode ->
                result = evaluateScopeContextForNode(element, scopeNode, result)
                links.add(scopeNode to result)
                if (scopeNode is ParadoxErrorScopeNode) return@run
            }
        }
        return inputScopeContext.resolveNext(links)
    }

    fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        when (node) {
            is ParadoxScopeNode -> {
                when (node) {
                    // parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // system -> context sensitive
                    is ParadoxSystemScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // predefined -> static
                    is ParadoxStaticScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // error -> unknown
                    is ParadoxErrorScopeNode -> {
                        return ParadoxScopeContext.resolveUnknown(inputScopeContext)
                    }
                }
            }
            is ParadoxScopePrefixNode -> {
                return evaluateScopeContextForNode(element, node, inputScopeContext)
            }
            is ParadoxCommandScopeNode -> {
                when (node) {
                    // parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedCommandScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // system -> context sensitive
                    is ParadoxSystemCommandScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // predefined -> static (with promotions)
                    is ParadoxStaticCommandScopeNode -> {
                        val linkConfig = node.config
                        val promotions = linkConfig.configGroup.localisationPromotions[linkConfig.name]?.supportedScopes
                        val next = inputScopeContext.resolveNext(linkConfig.outputScope)
                        if (promotions.isNotNullOrEmpty()) next.promotions = promotions
                        return next
                    }
                    // dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicCommandScopeNode -> {
                        return inputScopeContext.resolveNext(ParadoxScopeContext.resolveAny())
                    }
                    // error -> unknown
                    is ParadoxErrorCommandScopeNode -> {
                        return ParadoxScopeContext.resolveUnknown(inputScopeContext)
                    }
                }
            }
            is ParadoxCommandFieldNode -> {
                return inputScopeContext
            }
        }
        return ParadoxScopeContext.resolveUnknown(inputScopeContext)
    }

    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxParameterizedNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        run r1@{
            // only support full parameterized node
            if (!node.text.isParameterized(full = true)) return@r1

            val startOffset = ParadoxExpressionService.getExpressionOffset(element) + node.rangeInExpression.startOffset
            val parameter = element.findElementAt(startOffset)?.parentOfType<ParadoxParameter>() ?: return@r1
            if (parameter.text != node.text) return@r1
            val parameterElement = ParadoxParameterManager.getParameterElement(parameter) ?: return@r1
            val configGroup = node.configGroup
            val configs = configGroup.extendedParameters.findByPattern(parameterElement.name, parameterElement, configGroup).orEmpty()
            val config = configs.findLastFast { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) } ?: return@r1
            val contextContainerConfig = ParadoxExtendedConfigService.getContextContainerConfig(config, parameterElement)

            // ex_param = scope[country]
            // result: country (don't validate & inline allowed)
            run r2@{
                val inferredScope = contextContainerConfig.castOrNull<CwtPropertyConfig>()?.valueExpression
                    ?.takeIf { it.type == CwtDataTypes.Scope }?.metadata?.value?.orNull() ?: return@r2
                return inputScopeContext.resolveNext(inferredScope)
            }

            // ## push_scope = country
            // ex_param = ...
            // result: country (don't validate & inline allowed)
            run r2@{
                val inferredScopeContext = ParadoxScopeManager.getScopeContext(contextContainerConfig, inputScopeContext) ?: return@r2
                return inferredScopeContext
            }
        }
        return inputScopeContext.resolveNext(ParadoxScopeContext.resolveAny())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxSystemScopeAwareLinkNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val systemScopeConfig = node.config
        val id = systemScopeConfig.name
        val baseId = systemScopeConfig.base
        val isFrom = baseId == "From"
        val systemScopeContext = when {
            id == "This" -> inputScopeContext
            id == "Root" -> inputScopeContext.root
            id == "Prev" -> inputScopeContext.prev
            id == "PrevPrev" -> inputScopeContext.prev2
            id == "PrevPrevPrev" -> inputScopeContext.prev3
            id == "PrevPrevPrevPrev" -> inputScopeContext.prev4
            id == "From" -> inputScopeContext.from
            id == "FromFrom" -> inputScopeContext.from2
            id == "FromFromFrom" -> inputScopeContext.from3
            id == "FromFromFromFrom" -> inputScopeContext.from4
            else -> null
        }
        if (systemScopeContext == null) return ParadoxScopeContext.resolveUnknown(inputScopeContext, isFrom)
        return inputScopeContext.resolveNext(systemScopeContext, isFrom)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxLinkPrefixNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return ParadoxScopeContext.resolveUnknown(inputScopeContext)
        return inputScopeContext.resolveNext(linkConfig.outputScope)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxStaticScopeNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val outputScope = node.config.outputScope
        return inputScopeContext.resolveNext(outputScope)
    }

    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxDynamicScopeNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return ParadoxScopeContext.resolveUnknown(inputScopeContext)
        if (linkConfig.outputScope != null) return inputScopeContext.resolveNext(linkConfig.outputScope)

        // output_scope = null -> transfer scope based on data source
        val dataType = linkConfig.configExpression?.type
        if (dataType == null) return inputScopeContext
        when {
            // hidden:event_target:xxx = {...}
            dataType in CwtDataTypeSets.ScopeField -> {
                val nestedNode = node.valueNode.nodes.findIsInstanceFast<ParadoxScopeNode>()
                    ?: return ParadoxScopeContext.resolveUnknown(inputScopeContext)
                return evaluateScopeContextForNode(element, nestedNode, inputScopeContext)
            }
            // event_target:xxx = {...}
            dataType in CwtDataTypeSets.DynamicValue -> {
                val dynamicValueExpression = node.valueNode.nodes.findIsInstanceFast<ParadoxDynamicValueExpression>()
                    ?: return ParadoxScopeContext.resolveUnknown(inputScopeContext)
                val configGroup = dynamicValueExpression.configGroup
                val dynamicValueNode = dynamicValueExpression.dynamicValueNode
                val name = dynamicValueNode.text
                val expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    else -> element.castOrNull<ParadoxScriptStringExpressionElement>()
                }
                if (expressionElement == null) return ParadoxScopeContext.resolveAny()
                val configExpressions = dynamicValueNode.configs.mapNotNullFast { it.configExpression } // delay distinct
                val dynamicValueElement = ParadoxDynamicValueManager.resolveDynamicValue(expressionElement, name, configExpressions, configGroup)
                if (dynamicValueElement == null) return ParadoxScopeContext.resolveAny()
                return ParadoxScopeManager.getScopeContext(dynamicValueElement, inputScopeContext)
            }
            // unexpected, or other specific situations
            else -> {
                return inputScopeContext
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun evaluateSupportedScopesForNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): Set<String>? {
        when (node) {
            is ParadoxCommandScopeNode -> {
                when (node) {
                    // system -> any
                    is ParadoxSystemCommandScopeNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                    // predefined -> static
                    is ParadoxStaticCommandScopeNode -> {
                        return node.config.inputScopes
                    }
                    // parameterized -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandScopeNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandScopeNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                    // error -> any
                    is ParadoxErrorCommandScopeNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                }
            }
            is ParadoxCommandFieldNode -> {
                when (node) {
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandFieldNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                    // predefined -> static
                    is ParadoxStaticCommandFieldNode -> {
                        return node.config.supportedScopes
                    }
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandFieldNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                    // error -> any
                    is ParadoxErrorCommandFieldNode -> {
                        return ParadoxScopeConstants.anyScopes
                    }
                }
            }
        }
        return null
    }
}
