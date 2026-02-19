package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.isProperty
import icu.windea.pls.config.resolved
import icu.windea.pls.config.resolvedOrNull
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionInferredScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionSupportedScopesProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDynamicValueInferredScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDynamicValueScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxOverriddenScopeContextProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.dynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeAwareLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.resolve.complexExpression.scopeNodes
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScopeManager.findParentMember
import icu.windea.pls.lang.util.manipulators.ParadoxScopeManipulator
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeId
import icu.windea.pls.model.scope.isExact
import icu.windea.pls.model.scope.overriddenProvider
import icu.windea.pls.model.scope.promotions
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxScopeService {
    /**
     * @see ParadoxDefinitionSupportedScopesProvider.getSupportedScopes
     */
    fun getSupportedScopes(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionSupportedScopesProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(definition, definitionInfo)) return@f null
            ep.getSupportedScopes(definition, definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionScopeContextProvider.getScopeContext
     */
    fun getScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(definition, definitionInfo)) return@f null
            ep.getScopeContext(definition, definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        var map: Map<String, String>? = null
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeManipulator.mergeScopeContextMap(map, info.scopeContextMap)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.get(resultMap)
        return result
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getMessage
     */
    @Suppress("unused")
    fun getInferenceMessage(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        var message: String? = null
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (info.hasConflict) return@f
            if (message == null) {
                message = ep.getMessage(definition, definitionInfo, info)
            } else {
                return PlsBundle.message("script.annotator.scopeContext", definitionInfo.name)
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
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (!info.hasConflict) return@f
            if (errorMessage == null) {
                errorMessage = ep.getErrorMessage(definition, definitionInfo, info)
            } else {
                return PlsBundle.message("script.annotator.scopeContext.conflict", definitionInfo.name)
            }
        }
        return errorMessage
    }

    /**
     * @see ParadoxDynamicValueScopeContextProvider.getScopeContext
     */
    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val gameType = element.gameType
        return ParadoxDynamicValueScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(element)) return@f null
            ep.getScopeContext(element)
        }
    }

    /**
     * @see ParadoxDynamicValueInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(dynamicValue: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val gameType = dynamicValue.gameType
        var map: Map<String, String>? = null
        ParadoxDynamicValueInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(dynamicValue)) return@f
            val info = ep.getScopeContext(dynamicValue) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeManipulator.mergeScopeContextMap(map, info.scopeContextMap)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.get(resultMap)
        return result
    }

    /**
     * @see ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext
     */
    fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        val gameType = config.configGroup.gameType
        return ParadoxOverriddenScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getOverriddenScopeContext(contextElement, config, parentScopeContext)
                ?.also { it.overriddenProvider = ep }
        }
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
        if (definitionType in configGroup.definitionTypesModel.supportScope) return true
        if (indirect && definitionType in configGroup.definitionTypesModel.indirectSupportScope) return true
        return false
    }

    private fun isScopeContextSupportedForDefinitionMember(element: ParadoxScriptMember): Boolean {
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(acceptDefinition = true))
        if (configs.isEmpty()) return false
        return configs.any { isScopeContextSupportedFromConfig(it) }
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
        for (it in properties) {
            val aliasName = when {
                it.keyExpression.type == CwtDataTypes.AliasName -> it.keyExpression.value
                else -> continue
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
        if (inferredScopeContext != null) return ParadoxScopeManipulator.mergeScopeContext(scopeContext, inferredScopeContext) ?: ParadoxScopeContext.getAny()

        return scopeContext ?: ParadoxScopeContext.getAny()
    }

    private fun evaluateScopeContextForDefinitionMember(element: ParadoxScriptMember): ParadoxScopeContext? {
        // element could be a definition member only if after inlined
        val parentMember = findParentMember(element, withSelf = false)
        val parentScopeContext = if (parentMember != null) ParadoxScopeManager.getScopeContext(parentMember) else null
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(acceptDefinition = true))
        val config = configs.firstOrNull() ?: return null

        val overriddenScopeContext = getOverriddenScopeContext(element, config, parentScopeContext)
        if (overriddenScopeContext != null) return overriddenScopeContext

        if (config.isProperty() && config.configExpression.type == CwtDataTypes.ScopeField) {
            if (parentScopeContext == null) return null
            val expressionElement = element.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return null
            val expressionString = expressionElement.value
            val configGroup = config.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, null, configGroup) ?: return null
            val result = ParadoxScopeManager.getScopeContext(expressionElement, scopeFieldExpression, parentScopeContext)
            return result
        } else {
            // 优先基于内联前的规则，如果没有，再基于内联后的规则
            val replaceScopes = config.optionData.replaceScopes ?: config.resolvedOrNull()?.optionData?.replaceScopes
            val pushScope = config.optionData.pushScope ?: config.resolved().optionData.pushScope
            val scopeContext = replaceScopes?.let { ParadoxScopeContext.get(it) } ?: parentScopeContext ?: return null
            val result = scopeContext.resolveNext(pushScope)
            return result
        }
    }

    fun evaluateScopeContextForDynamicValue(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        // get provided scope context from EPs, and use it if not exact
        val scopeContext = getScopeContext(element)
        if (scopeContext != null && scopeContext.isExact) return scopeContext

        // get inferred scope context from EPs, and use the merged result if exists
        val inferredScopeContext = getInferredScopeContext(element)
        if (inferredScopeContext != null) return ParadoxScopeManipulator.mergeScopeContext(scopeContext, inferredScopeContext) ?: ParadoxScopeContext.getAny()

        return scopeContext ?: ParadoxScopeContext.getAny()
    }

    fun evaluateScopeContextForExpression(element: ParadoxScriptMember, expression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxScopeContext? {
        val parentElement = findParentMember(element, withSelf = false)
        val parentScopeContext = when {
            parentElement != null -> ParadoxScopeManager.getScopeContext(parentElement) ?: ParadoxScopeContext.getAny()
            else -> ParadoxScopeContext.getAny()
        }
        val expressionElement = when {
            element is ParadoxScriptProperty -> if (configExpression.isKey) element.propertyKey else element.propertyValue
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
        val links = mutableListOf<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>()
        for (scopeNode in scopeNodes) {
            result = evaluateScopeContextForNode(element, scopeNode, result)
            links.add(scopeNode to result)
            if (scopeNode is ParadoxErrorScopeLinkNode) break
        }
        return inputScopeContext.resolveNext(links)
    }

    fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        when (node) {
            is ParadoxScopeLinkNode -> {
                when (node) {
                    // parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedScopeLinkNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // system -> context sensitive
                    is ParadoxSystemScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // predefined -> static
                    is ParadoxScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicScopeLinkNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // error -> unknown
                    is ParadoxErrorScopeLinkNode -> {
                        return ParadoxScopeContext.getUnknown(inputScopeContext)
                    }
                }
            }
            is ParadoxScopeLinkPrefixNode -> {
                return evaluateScopeContextForNode(element, node, inputScopeContext)
            }
            is ParadoxCommandScopeLinkNode -> {
                when (node) {
                    // parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedCommandScopeLinkNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // system -> context sensitive
                    is ParadoxSystemCommandScopeNode -> {
                        return evaluateScopeContextForNode(element, node, inputScopeContext)
                    }
                    // predefined -> static (with promotions)
                    is ParadoxCommandScopeNode -> {
                        val linkConfig = node.config
                        val promotions = linkConfig.configGroup.localisationPromotions[linkConfig.name]?.supportedScopes
                        val next = inputScopeContext.resolveNext(linkConfig.outputScope)
                        if (promotions.isNotNullOrEmpty()) next.promotions = promotions
                        return next
                    }
                    // dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicCommandScopeLinkNode -> {
                        return inputScopeContext.resolveNext(ParadoxScopeContext.getAny())
                    }
                    // error -> unknown
                    is ParadoxErrorCommandScopeLinkNode -> {
                        return ParadoxScopeContext.getUnknown(inputScopeContext)
                    }
                }
            }
            is ParadoxCommandFieldNode -> {
                return inputScopeContext
            }
        }
        return ParadoxScopeContext.getUnknown(inputScopeContext)
    }

    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxParameterizedNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        run r1@{
            // only support full parameterized node
            if (!node.text.isParameterized(full = true)) return@r1

            val offset = node.rangeInExpression.startOffset
            val parameter = element.findElementAt(offset)?.parentOfType<ParadoxParameter>() ?: return@r1
            if (parameter.text != node.text) return@r1
            val parameterElement = ParadoxParameterManager.getParameterElement(parameter) ?: return@r1
            val configGroup = node.configGroup
            val configs = configGroup.extendedParameters.findByPattern(parameterElement.name, parameterElement, configGroup).orEmpty()
            val config = configs.findLast { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) } ?: return@r1
            val containerConfig = config.getContainerConfig(parameterElement)

            // ex_param = scope[country]
            // result: country (don't validate & inline allowed)
            run r2@{
                val inferredScope = containerConfig.castOrNull<CwtPropertyConfig>()?.valueExpression
                    ?.takeIf { it.type == CwtDataTypes.Scope }
                    ?.value?.orNull() ?: return@r2
                return inputScopeContext.resolveNext(inferredScope)
            }

            // ## push_scope = country
            // ex_param = ...
            // result: country (don't validate & inline allowed)
            run r2@{
                val inferredScopeContext = ParadoxScopeManager.getScopeContext(containerConfig, inputScopeContext) ?: return@r2
                return inferredScopeContext
            }
        }
        return inputScopeContext.resolveNext(ParadoxScopeContext.getAny())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxSystemScopeAwareLinkNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val systemScopeConfig = node.config
        val id = systemScopeConfig.id
        val baseId = systemScopeConfig.baseId
        val isFrom = baseId == "From"
        val systemScopeContext = when {
            id == "This" -> inputScopeContext
            id == "Root" -> inputScopeContext.root
            id == "Prev" -> inputScopeContext.prev
            id == "PrevPrev" -> inputScopeContext.prevPrev
            id == "PrevPrevPrev" -> inputScopeContext.prevPrevPrev
            id == "PrevPrevPrevPrev" -> inputScopeContext.prevPrevPrevPrev
            id == "From" -> inputScopeContext.from
            id == "FromFrom" -> inputScopeContext.fromFrom
            id == "FromFromFrom" -> inputScopeContext.fromFromFrom
            id == "FromFromFromFrom" -> inputScopeContext.fromFromFromFrom
            else -> null
        }
        if (systemScopeContext == null) return ParadoxScopeContext.getUnknown(inputScopeContext, isFrom)
        return inputScopeContext.resolveNext(systemScopeContext, isFrom)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxLinkPrefixNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return ParadoxScopeContext.getUnknown(inputScopeContext)
        return inputScopeContext.resolveNext(linkConfig.outputScope)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxScopeNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val outputScope = node.config.outputScope
        return inputScopeContext.resolveNext(outputScope)
    }

    private fun evaluateScopeContextForNode(element: ParadoxExpressionElement, node: ParadoxDynamicScopeLinkNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return ParadoxScopeContext.getUnknown(inputScopeContext)
        if (linkConfig.outputScope != null) return inputScopeContext.resolveNext(linkConfig.outputScope)

        // output_scope = null -> transfer scope based on data source
        val dataType = linkConfig.configExpression?.type
        if (dataType == null) return inputScopeContext
        when {
            // hidden:event_target:xxx = {...}
            dataType in CwtDataTypeSets.ScopeField -> {
                val nestedNode = node.valueNode.nodes.findIsInstance<ParadoxScopeLinkNode>()
                    ?: return ParadoxScopeContext.getUnknown(inputScopeContext)
                return evaluateScopeContextForNode(element, nestedNode, inputScopeContext)
            }
            // event_target:xxx = {...}
            dataType in CwtDataTypeSets.DynamicValue -> {
                val dynamicValueExpression = node.valueNode.nodes.findIsInstance<ParadoxDynamicValueExpression>()
                    ?: return ParadoxScopeContext.getUnknown(inputScopeContext)
                val configGroup = dynamicValueExpression.configGroup
                val dynamicValueNode = dynamicValueExpression.dynamicValueNode
                val name = dynamicValueNode.text
                val configExpressions = dynamicValueNode.configs.mapNotNullTo(mutableSetOf()) { it.configExpression }
                val expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    else -> element.castOrNull<ParadoxScriptStringExpressionElement>()
                }
                if (expressionElement == null) return ParadoxScopeContext.getAny()
                val dynamicValueElement = ParadoxDynamicValueManager.resolveDynamicValue(expressionElement, name, configExpressions, configGroup)
                if (dynamicValueElement == null) return ParadoxScopeContext.getAny()
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
            is ParadoxCommandScopeLinkNode -> {
                when (node) {
                    // system -> any
                    is ParadoxSystemCommandScopeNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                    // predefined -> static
                    is ParadoxCommandScopeNode -> {
                        return node.config.inputScopes
                    }
                    // parameterized -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandScopeLinkNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandScopeLinkNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                    // error -> any
                    is ParadoxErrorCommandScopeLinkNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                }
            }
            is ParadoxCommandFieldNode -> {
                when (node) {
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandFieldNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                    // predefined -> static
                    is ParadoxPredefinedCommandFieldNode -> {
                        return node.config.supportedScopes
                    }
                    // dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandFieldNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                    // error -> any
                    is ParadoxErrorCommandFieldNode -> {
                        return ParadoxScopeId.anyScopeIdSet
                    }
                }
            }
        }
        return null
    }
}
