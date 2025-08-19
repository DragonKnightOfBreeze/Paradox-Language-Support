package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理作用域。
 */
@Suppress("UNUSED_PARAMETER")
object ParadoxScopeManager {
    object Keys : KeyRegistry() {
        val cachedScopeContext by createKey<CachedValue<ParadoxScopeContext>>(Keys)
    }

    const val maxScopeLinkSize = 5

    const val unknownScopeId = "?"
    const val anyScopeId = "any"
    const val allScopeId = "all"

    val anyScopeIdSet = setOf(anyScopeId)

    /**
     * 得到作用域的ID（全小写+下划线）。
     */
    fun getScopeId(scope: String): String {
        val scopeId = scope.lowercase().replace(' ', '_').intern() //intern to optimize memory
        //"all" scope are always resolved as "any" scope
        if (scopeId == allScopeId) return anyScopeId
        return scopeId
    }

    /**
     * 得到作用域的名字。
     */
    fun getScopeName(scope: String, configGroup: CwtConfigGroup): String {
        //handle "any" and "all" scope
        if (scope.equals(anyScopeId, true)) return "Any"
        if (scope.equals(allScopeId, true)) return "All"
        //a scope may not have aliases, or not defined in scopes.cwt
        return configGroup.scopes[scope]?.name
            ?: configGroup.scopeAliasMap[scope]?.name
            ?: scope.toCapitalizedWords().intern() //intern to optimize memory
    }

    fun isUnsureScopeId(scopeId: String): Boolean {
        return scopeId == unknownScopeId || scopeId == anyScopeId || scopeId == allScopeId
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (scopeToMatch == anyScopeId) return true
        if (thisScope == anyScopeId) return true
        if (thisScope == unknownScopeId) return true
        if (thisScope == scopeToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if (scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true

        val promotions = scopeContext.promotions
        for (promotion in promotions) {
            if (promotion == scopeToMatch) return true
            val promotionConfig = configGroup.scopeAliasMap[promotion]
            if (promotionConfig != null && promotionConfig.aliases.any { it == scopeToMatch }) return true
        }

        return false
    }

    fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (scopesToMatch.isNullOrEmpty() || scopesToMatch == anyScopeIdSet) return true
        if (thisScope == anyScopeId) return true
        if (thisScope == unknownScopeId) return true
        if (thisScope in scopesToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if (scopeConfig != null) return scopeConfig.aliases.any { it in scopesToMatch }

        val promotions = scopeContext.promotions
        for (promotion in promotions) {
            if (promotion in scopesToMatch) return true
            val promotionConfig = configGroup.scopeAliasMap[promotion]
            if (promotionConfig != null && promotionConfig.aliases.any { it in scopesToMatch }) return true
        }

        return false
    }

    fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if (thisScope == null) return true
        if (thisScope == anyScopeId) return true
        if (thisScope == unknownScopeId) return true
        val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch] ?: return false
        for (scopeToMatch in scopeGroupConfig.values) {
            if (thisScope == scopeToMatch) return true
            val scopeConfig = configGroup.scopeAliasMap[thisScope]
            if (scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true
        }
        return false //cwt config error
    }

    fun findParentMember(element: PsiElement, withSelf: Boolean): ParadoxScriptMemberElement? {
        return element.parents(withSelf)
            .find { it is ParadoxScriptDefinitionElement || (it is ParadoxScriptBlock && it.isBlockMember()) }
            .castOrNull<ParadoxScriptMemberElement>()
    }

    /**
     * @param indirect 是否包括间接支持作用域上下文的情况。（如事件）
     */
    fun isScopeContextSupported(element: ParadoxScriptMemberElement, indirect: Boolean = false): Boolean {
        //some definitions, such as on_action, also support scope context on definition level
        if (element is ParadoxScriptDefinitionElement) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo != null) {
                val configGroup = definitionInfo.configGroup
                val definitionType = definitionInfo.type
                if (definitionType in configGroup.definitionTypesSupportScope) return true
                if (indirect && definitionType in configGroup.definitionTypesIndirectSupportScope) return true
            }
        }

        //child config can be "alias_name[X] = ..." and "alias[X:scope_field]" is valid
        //or root config in config tree is "alias[X:xxx] = ..." and "alias[X:scope_field]" is valid
        val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
        configs.forEach { config ->
            val configGroup = config.configGroup
            if (config.configExpression.type == CwtDataTypes.AliasKeysField) return true
            if (isScopeContextSupportedAsRoot(config, configGroup)) return true
            if (isScopeContextSupportedAsChild(config, configGroup)) return true
        }

        //if there is an overridden scope context, so do supported
        val scopeContext = getSwitchedScopeContext(element)
        if (scopeContext?.overriddenProvider != null) return true

        return false
    }

    private fun isScopeContextSupportedAsRoot(config: CwtMemberConfig<*>, configGroup: CwtConfigGroup): Boolean {
        val properties = config.properties ?: return false
        return properties.any {
            val aliasName = when {
                it.keyExpression.type == CwtDataTypes.AliasName -> it.keyExpression.value
                else -> null
            }
            aliasName != null && aliasName in configGroup.aliasNamesSupportScope
        }
    }

    private fun isScopeContextSupportedAsChild(config: CwtMemberConfig<*>, configGroup: CwtConfigGroup): Boolean {
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

    fun isScopeContextChanged(element: ParadoxScriptMemberElement, scopeContext: ParadoxScopeContext): Boolean {
        //does not have scope context -> changed always
        val parentMember = findParentMember(element, withSelf = false)
        if (parentMember == null) return true
        val parentScopeContext = getSwitchedScopeContext(parentMember)
        if (parentScopeContext == null) return true
        if (parentScopeContext != scopeContext) return true
        if (!isScopeContextSupported(parentMember)) return true
        return false
    }

    fun getSwitchedScopeContext(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        return doGetSwitchedScopeContextFromCache(element)
    }

    private fun doGetSwitchedScopeContextFromCache(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedScopeContext) {
            ProgressManager.checkCanceled()
            val value = doGetSwitchedScopeContextOfDefinition(element)
                ?: doGetSwitchedScopeContextOfDefinitionMember(element)
            value.withDependencyItems(
                element.containingFile,
                ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker, //from inference
            )
        }
    }

    private fun doGetSwitchedScopeContextOfDefinition(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //should be a definition
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if (definitionInfo != null) {
            element as ParadoxScriptDefinitionElement

            //使用提供的作用域上下文
            val scopeContext = ParadoxDefinitionScopeContextProvider.getScopeContext(element, definitionInfo)
            if (scopeContext != null && scopeContext.isExact) return scopeContext

            //除非提供的作用域上下文是准确的，否则再尝试获取推断得到的作用域上下文，并进行合并
            val inferredScopeContext = ParadoxDefinitionInferredScopeContextProvider.getScopeContext(element, definitionInfo)
            if (inferredScopeContext != null) return mergeScopeContext(scopeContext, inferredScopeContext) ?: getAnyScopeContext()

            return scopeContext ?: getAnyScopeContext()
        }
        return null
    }

    private fun doGetSwitchedScopeContextOfDefinitionMember(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //element could be a definition member only if after inlined

        val parentMember = findParentMember(element, withSelf = false)
        val parentScopeContext = if (parentMember != null) getSwitchedScopeContext(parentMember) else null
        val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
        val config = configs.firstOrNull() ?: return null

        val overriddenScopeContext = ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext(element, config, parentScopeContext)
        if (overriddenScopeContext != null) return overriddenScopeContext

        if (config is CwtPropertyConfig && config.configExpression.type == CwtDataTypes.ScopeField) {
            if (parentScopeContext == null) return null
            val expressionElement = element.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return null
            val expressionString = expressionElement.value
            val textRange = TextRange.create(0, expressionString.length)
            val configGroup = config.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return null
            val result = getSwitchedScopeContext(expressionElement, scopeFieldExpression, parentScopeContext)
            return result
        } else {
            //优先基于内联前的规则，如果没有，再基于内联后的规则
            val replaceScopes = config.replaceScopes ?: config.resolvedOrNull()?.replaceScopes
            val pushScope = config.pushScope ?: config.resolved().pushScope
            val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) } ?: parentScopeContext ?: return null
            val result = scopeContext.resolveNext(pushScope)
            return result
        }
    }

    fun getSwitchedScopeContext(element: ParadoxDynamicValueElement, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        //only receive push scope (this scope), ignore others (like root scope, etc.)
        val scopeContext = doGetSwitchedScopeContextFromCache(element)
        return inputScopeContext.resolveNext(scopeContext.scope.id)
    }

    private fun doGetSwitchedScopeContextFromCache(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        return CachedValuesManager.getCachedValue(element, Keys.cachedScopeContext) {
            ProgressManager.checkCanceled()
            val value = doGetSwitchedScopeContext(element)
            CachedValueProvider.Result(value, element)
        }
    }

    private fun doGetSwitchedScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        //使用提供的作用域上下文
        val scopeContext = ParadoxDynamicValueScopeContextProvider.getScopeContext(element)
        if (scopeContext != null && scopeContext.isExact) return scopeContext

        //除非提供的作用域上下文是准确的，否则再尝试获取推断得到的作用域上下文，并进行合并
        val inferredScopeContext = ParadoxDynamicValueInferredScopeContextProvider.getScopeContext(element)
        if (inferredScopeContext != null) return mergeScopeContext(scopeContext, inferredScopeContext) ?: getAnyScopeContext()

        return scopeContext ?: getAnyScopeContext()
    }

    fun getSwitchedScopeContext(element: PsiElement, scopeFieldExpression: ParadoxScopeFieldExpression, configExpression: CwtDataExpression): ParadoxScopeContext? {
        val memberElement = findParentMember(element, withSelf = true)
        val parentMemberElement = findParentMember(element, withSelf = false)
        val parentScopeContext = when {
            parentMemberElement != null -> getSwitchedScopeContext(parentMemberElement) ?: getAnyScopeContext()
            else -> getAnyScopeContext()
        }
        val expressionElement = when {
            memberElement is ParadoxScriptProperty -> if (configExpression.isKey) memberElement.propertyKey else memberElement.propertyValue
            memberElement is ParadoxScriptValue -> memberElement
            else -> return null
        } ?: return null
        return getSwitchedScopeContext(expressionElement, scopeFieldExpression, parentScopeContext)
    }

    fun getSwitchedScopeContext(element: ParadoxExpressionElement, scopeFieldExpression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val scopeNodes = scopeFieldExpression.scopeNodes
        if (scopeNodes.isEmpty()) return inputScopeContext //unexpected -> unchanged
        var result = inputScopeContext
        val links = mutableListOf<Tuple2<ParadoxScopeLinkNode, ParadoxScopeContext>>()
        for (scopeNode in scopeNodes) {
            result = getSwitchedScopeContextOfNode(element, scopeNode, result)
            links.add(scopeNode to result)
            if (scopeNode is ParadoxErrorScopeLinkNode) break
        }
        return inputScopeContext.resolveNext(links)
    }

    fun getSwitchedScopeContextOfNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        when (node) {
            is ParadoxScopeLinkNode -> {
                when (node) {
                    //parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedScopeLinkNode -> {
                        return getSwitchedScopeContextOfParameterizedScopeLinkNode(element, node, inputScopeContext)
                    }
                    //system -> context sensitive
                    is ParadoxSystemScopeNode -> {
                        return getSwitchedScopeContextOfSystemScopeLinkNode(element, node, inputScopeContext)
                    }
                    //predefined -> static
                    is ParadoxScopeNode -> {
                        return getScopeContextOfScopeLinkNode(element, node, inputScopeContext)
                    }
                    //dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicScopeLinkNode -> {
                        return getScopeContextOfDynamicScopeLinkNode(element, node, inputScopeContext)
                    }
                    //error -> unknown
                    is ParadoxErrorScopeLinkNode -> {
                        return getUnknownScopeContext(inputScopeContext)
                    }
                }
            }
            is ParadoxScopeLinkPrefixNode -> {
                return getSwitchedScopeContextOfLinkPrefixNode(element, node, inputScopeContext)
            }
            is ParadoxCommandScopeLinkNode -> {
                when (node) {
                    //parameterized -> any (or inferred from extended configs)
                    is ParadoxParameterizedCommandScopeLinkNode -> {
                        return getSwitchedScopeContextOfParameterizedScopeLinkNode(element, node, inputScopeContext)
                    }
                    //system -> context sensitive
                    is ParadoxSystemCommandScopeNode -> {
                        return getSwitchedScopeContextOfSystemScopeLinkNode(element, node, inputScopeContext)
                    }
                    //predefined -> static (with promotions)
                    is ParadoxCommandScopeNode -> {
                        val linkConfig = node.config
                        val promotions = linkConfig.configGroup.localisationPromotions[linkConfig.name]?.supportedScopes
                        val next = inputScopeContext.resolveNext(linkConfig.outputScope)
                        if (promotions.isNotNullOrEmpty()) next.promotions = promotions
                        return next
                    }
                    //dynamic -> any (or inferred from extended configs)
                    is ParadoxDynamicCommandScopeLinkNode -> {
                        return inputScopeContext.resolveNext(getAnyScopeContext())
                    }
                    //error -> unknown
                    is ParadoxErrorCommandScopeLinkNode -> {
                        return getUnknownScopeContext(inputScopeContext)
                    }
                }
            }
            is ParadoxCommandFieldNode -> return inputScopeContext
            else -> return getUnknownScopeContext(inputScopeContext)
        }
    }

    private fun getSwitchedScopeContextOfParameterizedScopeLinkNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        if (node !is ParadoxParameterizedNode) return getUnknownScopeContext(inputScopeContext)
        run r1@{
            //only support full parameterized node
            if (!node.text.isParameterized(full = true)) return@r1

            val offset = node.rangeInExpression.startOffset
            val parameter = element.findElementAt(offset)?.parentOfType<ParadoxParameter>() ?: return@r1
            if (parameter.text != node.text) return@r1
            val parameterElement = ParadoxParameterManager.getParameterElement(parameter) ?: return@r1
            val configGroup = node.configGroup
            val configs = configGroup.extendedParameters.findFromPattern(parameterElement.name, parameterElement, configGroup).orEmpty()
            val config = configs.findLast { it.contextKey.matchFromPattern(parameterElement.contextKey, parameterElement, configGroup) } ?: return@r1
            val containerConfig = config.getContainerConfig(parameterElement)

            //ex_param = scope[country]
            //result: country (don't validate & inline allowed)
            run r2@{
                val inferredScope = containerConfig.castOrNull<CwtPropertyConfig>()?.valueExpression
                    ?.takeIf { it.type == CwtDataTypes.Scope }
                    ?.value?.orNull() ?: return@r2
                return inputScopeContext.resolveNext(inferredScope)
            }

            //## push_scope = country
            //ex_param = ...
            //result: country (don't validate & inline allowed)
            run r2@{
                val inferredScopeContext = getScopeContextFromConfigOptions(containerConfig, inputScopeContext) ?: return@r2
                return inferredScopeContext
            }
        }
        return inputScopeContext.resolveNext(getAnyScopeContext())
    }

    private fun getSwitchedScopeContextOfSystemScopeLinkNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val systemScopeConfig = when {
            node is ParadoxSystemScopeNode -> node.config
            node is ParadoxSystemCommandScopeNode -> node.config
            else -> return getUnknownScopeContext(inputScopeContext)
        }
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
        } ?: return getUnknownScopeContext(inputScopeContext, isFrom)
        return inputScopeContext.resolveNext(systemScopeContext, isFrom)
    }

    private fun getSwitchedScopeContextOfLinkPrefixNode(element: ParadoxExpressionElement, node: ParadoxLinkPrefixNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return getUnknownScopeContext(inputScopeContext)
        return inputScopeContext.resolveNext(linkConfig.outputScope)
    }

    private fun getScopeContextOfScopeLinkNode(element: ParadoxExpressionElement, node: ParadoxScopeNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val outputScope = node.config.outputScope
        return inputScopeContext.resolveNext(outputScope)
    }

    private fun getScopeContextOfDynamicScopeLinkNode(element: ParadoxExpressionElement, node: ParadoxDynamicScopeLinkNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() ?: return getUnknownScopeContext(inputScopeContext)
        if (linkConfig.outputScope != null) return inputScopeContext.resolveNext(linkConfig.outputScope)

        //output_scope = null -> transfer scope based on data source
        val dataType = linkConfig.configExpression?.type
        if (dataType == null) return inputScopeContext
        when {
            //hidden:event_target:xxx = {...}
            dataType in CwtDataTypeGroups.ScopeField -> {
                val nestedNode = node.valueNode.nodes.findIsInstance<ParadoxScopeLinkNode>()
                    ?: return getUnknownScopeContext(inputScopeContext)
                return getSwitchedScopeContextOfNode(element, nestedNode, inputScopeContext)
            }
            //event_target:xxx = {...}
            dataType in CwtDataTypeGroups.DynamicValue -> {
                val dynamicValueExpression = node.valueNode.nodes.findIsInstance<ParadoxDynamicValueExpression>()
                    ?: return getUnknownScopeContext(inputScopeContext)
                val configGroup = dynamicValueExpression.configGroup
                val dynamicValueNode = dynamicValueExpression.dynamicValueNode
                val name = dynamicValueNode.text
                val configExpressions = dynamicValueNode.configs.mapNotNullTo(mutableSetOf()) { it.configExpression }
                val expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    else -> element.castOrNull<ParadoxScriptStringExpressionElement>()
                }
                if (expressionElement == null) return getAnyScopeContext()
                val dynamicValueElement = ParadoxDynamicValueManager.resolveDynamicValue(expressionElement, name, configExpressions, configGroup)
                if (dynamicValueElement == null) return getAnyScopeContext()
                return getSwitchedScopeContext(dynamicValueElement, inputScopeContext)
            }
            //unexpected, or other specific situations
            else -> {
                return inputScopeContext
            }
        }
    }

    fun getSupportedScopes(categoryConfigMap: Map<String, CwtModifierCategoryConfig>): Set<String> {
        val categoryConfigs = categoryConfigMap.values
        return when {
            categoryConfigs.isEmpty() -> anyScopeIdSet
            categoryConfigs.any { it.supportedScopes == anyScopeIdSet } -> anyScopeIdSet
            else -> categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
        }
    }

    fun getSupportedScopesOfNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, inputScopeContext: ParadoxScopeContext): Set<String>? {
        when (node) {
            is ParadoxCommandScopeLinkNode -> {
                when (node) {
                    //system -> any
                    is ParadoxSystemCommandScopeNode -> {
                        return anyScopeIdSet
                    }
                    //predefined -> static
                    is ParadoxCommandScopeNode -> {
                        return node.config.inputScopes
                    }
                    //parameterized -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandScopeLinkNode -> {
                        return anyScopeIdSet
                    }
                    //dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandScopeLinkNode -> {
                        return anyScopeIdSet
                    }
                    //error -> any
                    is ParadoxErrorCommandScopeLinkNode -> {
                        return anyScopeIdSet
                    }
                }
            }
            is ParadoxCommandFieldNode -> {
                when (node) {
                    //dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxParameterizedCommandFieldNode -> {
                        return anyScopeIdSet
                    }
                    //predefined -> static
                    is ParadoxPredefinedCommandFieldNode -> {
                        return node.config.supportedScopes
                    }
                    //dynamic -> any (NOTE cannot be inferred from extended configs, not supported yet)
                    is ParadoxDynamicCommandFieldNode -> {
                        return anyScopeIdSet
                    }
                    //error -> any
                    is ParadoxErrorCommandFieldNode -> {
                        return anyScopeIdSet
                    }
                }
            }
            else -> return null
        }
    }

    fun getScopeContextFromConfigOptions(config: CwtMemberConfig<*>, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
        //优先基于内联前的规则，如果没有，再基于内联后的规则
        val replaceScopes = config.replaceScopes ?: config.resolvedOrNull()?.replaceScopes
        val pushScope = config.pushScope ?: config.resolved().pushScope
        if (replaceScopes != null) {
            return ParadoxScopeContext.resolve(replaceScopes)
        } else if (pushScope != null) {
            return inputScopeContext.resolveNext(pushScope)
        }
        return null
    }

    fun getAnyScopeContext(): ParadoxScopeContext {
        return ParadoxScopeContext.resolve(anyScopeId, anyScopeId)
    }

    fun getUnknownScopeContext(inputScopeContext: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
        if (inputScopeContext == null) return ParadoxScopeContext.resolve(unknownScopeId)
        return inputScopeContext.resolveNext(unknownScopeId, isFrom)
    }

    fun mergeScopeId(scopeId: String?, otherScopeId: String?): String? {
        if (scopeId == otherScopeId) return scopeId
        if (scopeId == anyScopeId || otherScopeId == anyScopeId) return anyScopeId
        if (scopeId == unknownScopeId || otherScopeId == unknownScopeId) return unknownScopeId
        if (scopeId == null) return otherScopeId
        if (otherScopeId == null) return scopeId
        return null
    }

    fun mergeScope(scope: ParadoxScope?, otherScope: ParadoxScope?): ParadoxScope? {
        if (scope == otherScope) return scope ?: ParadoxScope.Unknown
        if (scope == ParadoxScope.Any || otherScope == ParadoxScope.Any) return ParadoxScope.Any
        if (scope == ParadoxScope.Unknown || otherScope == ParadoxScope.Unknown) return ParadoxScope.Unknown
        if (scope == null) return otherScope
        if (otherScope == null) return scope
        return null
    }

    fun mergeScopeContext(scopeContext: ParadoxScopeContext?, otherScopeContext: ParadoxScopeContext?, orUnknown: Boolean = false): ParadoxScopeContext? {
        val m1 = scopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val m2 = otherScopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val merged = mergeScopeContextMap(m1, m2, orUnknown) ?: return null
        return ParadoxScopeContext.resolve(merged)
    }

    fun mergeScopeContextMap(map: Map<String, String>, otherMap: Map<String, String>, orUnknown: Boolean = false): Map<String, String>? {
        val result = mutableMapOf<String, String>()
        mergeScopeId(map["this"], otherMap["this"])?.let { result["this"] = it }
        mergeScopeId(map["root"], otherMap["root"])?.let { result["root"] = it }
        mergeScopeId(map["prev"], otherMap["prev"])?.let { result["prev"] = it }
        mergeScopeId(map["prevprev"], otherMap["prevprev"])?.let { result["prevprev"] = it }
        mergeScopeId(map["prevprevprev"], otherMap["prevprevprev"])?.let { result["prevprevprev"] = it }
        mergeScopeId(map["prevprevprevprev"], otherMap["prevprevprevprev"])?.let { result["prevprevprevprev"] = it }
        mergeScopeId(map["from"], otherMap["from"])?.let { result["from"] = it }
        mergeScopeId(map["fromfrom"], otherMap["fromfrom"])?.let { result["fromfrom"] = it }
        mergeScopeId(map["fromfromfrom"], otherMap["fromfromfrom"])?.let { result["fromfromfrom"] = it }
        mergeScopeId(map["fromfromfromfrom"], otherMap["fromfromfromfrom"])?.let { result["fromfromfromfrom"] = it }
        if (orUnknown) {
            val thisScope = result["this"]
            if (thisScope == null || thisScope == unknownScopeId) {
                result["this"] = unknownScopeId
            }
            val rootScope = result["root"]
            if (rootScope == null || rootScope == unknownScopeId) {
                result["root"] = unknownScopeId
            }
        }
        return result.orNull()
    }
}
