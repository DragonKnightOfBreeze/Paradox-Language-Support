package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.complex.*
import icu.windea.pls.core.expression.complex.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.scope.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 用于处理作用域。
 */
@Suppress("UNUSED_PARAMETER")
object ParadoxScopeHandler {
    const val maxScopeLinkSize = 5
    
    const val unknownScopeId = "?"
    const val anyScopeId = "any"
    const val allScopeId = "all"
    
    val anyScopeIdSet = setOf(anyScopeId)
    
    /**
     * 得到作用域的ID（全小写+下划线）。
     */
    fun getScopeId(scope: String): String {
        val scopeId = scope.lowercase().replace(' ', '_')
        //"all" scope are always resolved as "any" scope
        if(scopeId == allScopeId) return anyScopeId
        return scopeId
    }
    
    /**
     * 得到作用域的名字。
     */
    fun getScopeName(scope: String, configGroup: CwtConfigGroup): String {
        //handle "any" and "all" scope 
        if(scope.equals(anyScopeId, true)) return "Any"
        if(scope.equals(allScopeId, true)) return "All"
        //a scope may not have aliases, or not defined in scopes.cwt
        return configGroup.scopes[scope]?.name
            ?: configGroup.scopeAliasMap[scope]?.name
            ?: scope.toCapitalizedWords()
    }
    
    fun isUnsureScopeId(scopeId: String): Boolean {
        return scopeId == unknownScopeId || scopeId == anyScopeId || scopeId == allScopeId
    }
    
    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if(thisScope == null) return true
        if(scopeToMatch == anyScopeId) return true
        if(thisScope == anyScopeId) return true
        if(thisScope == unknownScopeId) return true
        if(thisScope == scopeToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if(scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true
        return false
    }
    
    fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if(thisScope == null) return true
        if(scopesToMatch.isNullOrEmpty() || scopesToMatch == anyScopeIdSet) return true
        if(thisScope == anyScopeId) return true
        if(thisScope == unknownScopeId) return true
        if(thisScope in scopesToMatch) return true
        val scopeConfig = configGroup.scopeAliasMap[thisScope]
        if(scopeConfig != null) return scopeConfig.aliases.any { it in scopesToMatch }
        return false
    }
    
    fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val thisScope = scopeContext?.scope?.id
        if(thisScope == null) return true
        if(thisScope == anyScopeId) return true
        if(thisScope == unknownScopeId) return true
        val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch] ?: return false
        for(scopeToMatch in scopeGroupConfig.values) {
            if(thisScope == scopeToMatch) return true
            val scopeConfig = configGroup.scopeAliasMap[thisScope]
            if(scopeConfig != null && scopeConfig.aliases.any { it == scopeToMatch }) return true
        }
        return false //cwt config error
    }
    
    fun findParentMember(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
        return element.parents(withSelf = false)
            .find { it is ParadoxScriptDefinitionElement || (it is ParadoxScriptBlock && it.isBlockValue()) }
            .castOrNull<ParadoxScriptMemberElement>()
    }
    
    /**
     * @param indirect 是否包括间接支持作用域上下文的情况。（如事件）
     */
    fun isScopeContextSupported(element: ParadoxScriptMemberElement, indirect: Boolean = false): Boolean {
        //some definitions, such as on_action, also support scope context on definition level
        if(element is ParadoxScriptDefinitionElement) {
            val definitionInfo = element.definitionInfo
            if(definitionInfo != null) {
                val configGroup = definitionInfo.configGroup
                val definitionType = definitionInfo.type
                if(definitionType in configGroup.definitionTypesSupportScope) return true
                if(indirect && definitionType in configGroup.definitionTypesIndirectSupportScope) return true
            }
        }
        
        //child config can be "alias_name[X] = ..." and "alias[X:scope_field]" is valid
        //or root config in config tree is "alias[X:xxx] = ..." and "alias[X:scope_field]" is valid
        val configs = CwtConfigHandler.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
        configs.forEach { config ->
            val configGroup = config.info.configGroup
            if(config.expression.type == CwtDataTypes.AliasKeysField) return true
            if(isScopeContextSupportedAsRoot(config, configGroup)) return true
            if(isScopeContextSupportedAsChild(config, configGroup)) return true
        }
        
        //if there is an overridden scope context, so do supported
        val scopeContext = getScopeContext(element)
        if(scopeContext?.overriddenProvider != null) return true
        
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
        while(true) {
            if(currentConfig is CwtPropertyConfig) {
                val inlineableConfig = currentConfig.inlineableConfig
                if(inlineableConfig is CwtAliasConfig) {
                    val aliasName = inlineableConfig.name
                    if(aliasName in configGroup.aliasNamesSupportScope) return true
                }
            } else if(currentConfig is CwtValueConfig) {
                currentConfig = currentConfig.propertyConfig ?: currentConfig
            }
            currentConfig = currentConfig.parentConfig ?: break
        }
        return false
    }
    
    fun isScopeContextChanged(element: ParadoxScriptMemberElement, scopeContext: ParadoxScopeContext): Boolean {
        //does not have scope context -> changed always
        val parentMember = findParentMember(element)
        if(parentMember == null) return true
        val parentScopeContext = getScopeContext(parentMember)
        if(parentScopeContext == null) return true
        if(parentScopeContext != scopeContext) return true
        if(!isScopeContextSupported(parentMember)) return true
        return false
    }
    
    /**
     * 注意，如果输入的是值为子句的属性，这里得到的会是子句中的作用域上下文，而非此属性所在子句中的作用域上下文。
     */
    fun getScopeContext(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        return doGetScopeContextFromCache(element)
    }
    
    private fun doGetScopeContextFromCache(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContext) {
            val file = element.containingFile ?: return@getCachedValue null
            val value = doGetScopeContextOfDefinition(element)
                ?: doGetScopeContextOfDefinitionMember(element)
            val tracker = ParadoxModificationTrackerProvider.DefinitionScopeContextInferenceTracker
            CachedValueProvider.Result.create(value, file, tracker)
        }
    }
    
    private fun doGetScopeContextOfDefinition(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //should be a definition
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            element as ParadoxScriptDefinitionElement
            
            //使用提供的作用域上下文
            val scopeContext = ParadoxDefinitionScopeContextProvider.getScopeContext(element, definitionInfo)
            
            //如果推断得到的作用域上下文是确定的，则优先使用推断得到的
            val inferredScopeContext = ParadoxDefinitionInferredScopeContextProvider.getScopeContext(element, definitionInfo)
            
            val mergedScopeContext = mergeScopeContext(scopeContext, inferredScopeContext)
            if(mergedScopeContext != null) return mergedScopeContext
            
            return getAnyScopeContext()
        }
        return null
    }
    
    private fun doGetScopeContextOfDefinitionMember(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //should be a definition member
        val parentMember = findParentMember(element) ?: return null
        val parentScopeContext = getScopeContext(parentMember)
        val configs = CwtConfigHandler.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
        val config = configs.firstOrNull() ?: return null
        
        val overriddenScopeContext = ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext(element, config, parentScopeContext)
        if(overriddenScopeContext != null) return overriddenScopeContext
        
        if(config is CwtPropertyConfig && config.expression.type == CwtDataTypes.ScopeField) {
            if(parentScopeContext == null) return null
            val scopeField = element.castOrNull<ParadoxScriptProperty>()?.propertyKey?.text ?: return null
            if(scopeField.isLeftQuoted()) return null
            val textRange = TextRange.create(0, scopeField.length)
            val configGroup = config.info.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup) ?: return null
            val result = getScopeContext(element, scopeFieldExpression, parentScopeContext)
            return result
        } else {
            //优先基于内联前的规则，如果没有，再基于内联后的规则
            val scopeContext = config.scopeContext ?: config.resolved().scopeContext ?: parentScopeContext ?: return null
            val pushScope = config.pushScope ?: config.resolved().pushScope
            val result = scopeContext.resolveNext(pushScope)
            return result
        }
    }
    
    fun getScopeContext(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
        return doGetScopeContextFromCache(element)
    }
    
    private fun doGetScopeContextFromCache(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContext) {
            val file = element.containingFile ?: return@getCachedValue null
            val value = doGetScopeContextOfLocalisationCommandIdentifier(element)
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetScopeContextOfLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext {
        val prevElement = element.prevIdentifier
        val prevResolved = prevElement?.reference?.resolve()
        when {
            //system link or localisation scope
            prevResolved is CwtProperty -> {
                val config = prevResolved.getUserData(PlsKeys.cwtConfig)
                when(config) {
                    is CwtLocalisationLinkConfig -> {
                        val prevScopeContext = prevElement.prevIdentifier?.let { getScopeContext(it) } ?: getAnyScopeContext()
                        return prevScopeContext.resolveNext(config.outputScope)
                    }
                    is CwtSystemLinkConfig -> {
                        return getAnyScopeContext()
                    }
                    //predefined event target - no scope info in cwt files yet
                    is CwtValueConfig -> {
                        return getAnyScopeContext()
                    }
                }
            }
            prevResolved is ParadoxDynamicValueElement -> {
                val prevScopeContext = prevElement.prevIdentifier?.let { getScopeContext(it) } ?: getAnyScopeContext()
                val scopeContext = getScopeContext(prevResolved)
                return prevScopeContext.resolveNext(scopeContext)
            }
        }
        return getUnknownScopeContext()
    }
    
    fun getScopeContext(contextElement: PsiElement, scopeFieldExpression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val scopeNodes = scopeFieldExpression.scopeNodes
        var result = inputScopeContext
        val resolved = mutableListOf<Tuple2<ParadoxScopeFieldExpressionNode, ParadoxScopeContext>>()
        for((i, scopeNode) in scopeNodes.withIndex()) {
            val inExpression = i != 0
            result = getScopeContext(contextElement, scopeNode, result, inExpression)
            resolved.add(scopeNode to result)
            if(scopeNode is ParadoxErrorScopeFieldExpressionNode) break
        }
        result.scopeFieldInfo = resolved
        return result
    }
    
    fun getScopeContext(contextElement: PsiElement, node: ParadoxScopeFieldExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        return when(node) {
            is ParadoxScopeLinkExpressionNode -> {
                doGetScopeContextByScopeLinkNode(contextElement, node, inputScopeContext, inExpression)
            }
            is ParadoxScopeLinkFromDataExpressionNode -> {
                doGetScopeContextByScopeLinkFromDataNode(contextElement, node, inputScopeContext, inExpression)
            }
            is ParadoxSystemLinkExpressionNode -> {
                val isFrom = node.config.baseId.lowercase() == "from"
                doGetScopeContextBySystemLinkNode(contextElement, node, inputScopeContext, inExpression)
                    ?: getUnknownScopeContext(inputScopeContext, isFrom)
            }
            is ParadoxParameterizedScopeFieldExpressionNode -> getAnyScopeContext()
            //error
            is ParadoxErrorScopeFieldExpressionNode -> getUnknownScopeContext(inputScopeContext)
        }
    }
    
    fun getScopeContext(contextElement: PsiElement, node: ParadoxLinkPrefixExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() // first is ok
        if(linkConfig == null) return getUnknownScopeContext(inputScopeContext) //unexpected
        return inputScopeContext.resolveNext(linkConfig.outputScope)
    }
    
    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        return doGetScopeContextFromCache(element)
    }
    
    private fun doGetScopeContextFromCache(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContext) {
            val value = doGetScopeContextOfDynamicValue(element)
            val tracker = ModificationTracker.NEVER_CHANGED
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetScopeContextOfDynamicValue(element: ParadoxDynamicValueElement): ParadoxScopeContext {
        val scopeContext = ParadoxDynamicValueScopeContextProvider.getScopeContext(element)
        if(scopeContext != null) return scopeContext
        val inferredScopeContext = ParadoxDynamicValueInferredScopeContextProvider.getScopeContext(element)
        if(inferredScopeContext != null) return inferredScopeContext
        return getAnyScopeContext()
    }
    
    private fun doGetScopeContextByScopeLinkNode(contextElement: PsiElement, node: ParadoxScopeLinkExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        val outputScope = node.config.outputScope
        return inputScopeContext.resolveNext(outputScope)
    }
    
    private fun doGetScopeContextByScopeLinkFromDataNode(contextElement: PsiElement, node: ParadoxScopeLinkFromDataExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() // first is ok
        if(linkConfig == null) return getUnknownScopeContext(inputScopeContext) //unexpected
        if(linkConfig.outputScope == null) {
            val dataType = linkConfig.expression?.type
            if(dataType != null) {
                when {
                    //hidden:event_target:xxx = {...}
                    dataType in CwtDataTypeGroups.ScopeField -> {
                        val nestedNode = node.dataSourceNode.nodes.findIsInstance<ParadoxScopeFieldExpressionNode>()
                        if(nestedNode == null) return getUnknownScopeContext(inputScopeContext) //unexpected
                        return getScopeContext(contextElement, nestedNode, inputScopeContext, inExpression)
                    }
                    //event_target:xxx = {...}
                    dataType in CwtDataTypeGroups.DynamicValue -> {
                        val dynamicValueExpression = node.dataSourceNode.nodes.findIsInstance<ParadoxDynamicValueExpression>()
                        if(dynamicValueExpression == null) return getUnknownScopeContext(inputScopeContext) //unexpected
                        val configGroup = dynamicValueExpression.configGroup
                        val dynamicValueNode = dynamicValueExpression.dynamicValueNode
                        val name = dynamicValueNode.text
                        val configExpressions = dynamicValueNode.configs.mapNotNullTo(mutableSetOf()) { it.expression }
                        val expressionElement = contextElement.castOrNull<ParadoxScriptStringExpressionElement>() ?: return getAnyScopeContext()
                        val dynamicValueElement = ParadoxDynamicValueHandler.resolveDynamicValue(expressionElement, name, configExpressions, configGroup) ?: return getAnyScopeContext()
                        return getScopeContext(dynamicValueElement)
                    }
                }
            }
        }
        return inputScopeContext.resolveNext(linkConfig.outputScope)
    }
    
    private fun doGetScopeContextBySystemLinkNode(contextElement: PsiElement, node: ParadoxSystemLinkExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext? {
        val systemLinkConfig = node.config
        val id = systemLinkConfig.id
        val baseId = systemLinkConfig.baseId
        val systemLinkContext = when {
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
        } ?: return null
        val isFrom = baseId == "From"
        return inputScopeContext.resolveNext(systemLinkContext, isFrom)
    }
    
    fun getAnyScopeContext(): ParadoxScopeContext {
        return ParadoxScopeContext.resolve(anyScopeId, anyScopeId)
    }
    
    fun getUnknownScopeContext(inputScopeContext: ParadoxScopeContext? = null, isFrom: Boolean = false): ParadoxScopeContext {
        if(inputScopeContext == null) return ParadoxScopeContext.resolve(unknownScopeId)
        val resolved = inputScopeContext.resolveNext(unknownScopeId, isFrom)
        return resolved
    }
    
    fun getSupportedScopes(categoryConfigMap: Map<String, CwtModifierCategoryConfig>): Set<String> {
        val categoryConfigs = categoryConfigMap.values
        if(categoryConfigs.any { it.supportedScopes == anyScopeIdSet }) {
            return anyScopeIdSet
        } else {
            return categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
        }
    }
    
    fun buildScopeDoc(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement, builder: StringBuilder) {
        with(builder) {
            when {
                isUnsureScopeId(scopeId) -> append(scopeId)
                else -> appendCwtLink("${gameType.linkToken}scopes/$scopeId", scopeId, contextElement)
            }
        }
    }
    
    fun buildScopeContextDoc(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement, builder: StringBuilder) {
        with(builder) {
            var appendSeparator = false
            scopeContext.toScopeMap().forEach { (systemLink, scope) ->
                if(appendSeparator) appendBr() else appendSeparator = true
                appendCwtLink("${gameType.linkToken}system_links/$systemLink", systemLink, contextElement)
                append(" = ")
                when {
                    isUnsureScopeId(scope.id) -> append(scope)
                    else -> appendCwtLink("${gameType.linkToken}scopes/${scope.id}", scope.id, contextElement)
                }
            }
        }
    }
    
    fun mergeScopeId(scopeId: String?, otherScopeId: String?): String? {
        if(scopeId == otherScopeId) return scopeId ?: unknownScopeId
        if(scopeId == anyScopeId || otherScopeId == anyScopeId) return anyScopeId
        if(scopeId == unknownScopeId || otherScopeId == unknownScopeId) return unknownScopeId
        if(scopeId == null) return otherScopeId
        if(otherScopeId == null) return scopeId
        return null
    }
    
    fun mergeScope(scope: ParadoxScope?, otherScope: ParadoxScope?): ParadoxScope? {
        if(scope == otherScope) return scope ?: ParadoxScope.Unknown
        if(scope == ParadoxScope.Any || otherScope == ParadoxScope.Any) return ParadoxScope.Any
        if(scope == ParadoxScope.Unknown || otherScope == ParadoxScope.Unknown) return ParadoxScope.Unknown
        if(scope == null) return otherScope
        if(otherScope == null) return scope
        return null
    }
    
    fun mergeScopeContext(scopeContext: ParadoxScopeContext?, otherScopeContext: ParadoxScopeContext?, optimized: Boolean = false): ParadoxScopeContext? {
        val m1 = scopeContext?.toScopeIdMap(showPrev = false).orEmpty()
        val m2 = otherScopeContext?.toScopeIdMap(showFrom = false).orEmpty()
        val merged = mergeScopeContextMap(m1, m2, optimized) ?: return null
        return ParadoxScopeContext.resolve(merged)
    }
    
    fun mergeScopeContextMap(map: Map<String, String>, otherMap: Map<String, String>, optimized: Boolean = false): Map<String, String>? {
        val result = mutableMapOf<String, String>()
        doMergeScopeContextMap(result, map, otherMap, "this", true).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "root", true).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "prev", false)
        doMergeScopeContextMap(result, map, otherMap, "prevprev", false)
        doMergeScopeContextMap(result, map, otherMap, "prevprevprev", false)
        doMergeScopeContextMap(result, map, otherMap, "prevprevprevprev", false)
        doMergeScopeContextMap(result, map, otherMap, "from", false)
        doMergeScopeContextMap(result, map, otherMap, "fromfrom", false)
        doMergeScopeContextMap(result, map, otherMap, "fromfromfrom", false)
        doMergeScopeContextMap(result, map, otherMap, "fromfromfromfrom", false)
        if(optimized) doOptimizeScopeMap(result)
        return result.orNull()
    }
    
    private fun doMergeScopeContextMap(result: MutableMap<String, String>, m1: Map<String, String>, m2: Map<String, String>, key: String, orUnknown: Boolean): Boolean {
        val s = mergeScopeId(m1[key], m2[key])
        val r = if(orUnknown) s ?: unknownScopeId else s.takeUnless { it == unknownScopeId }
        if(r != null) result[key] = r
        return r != null
    }
    
    private fun doOptimizeScopeMap(scopeMap: MutableMap<String, String>) {
        val thisScope = scopeMap["this"]
        if(thisScope == null || thisScope == unknownScopeId) {
            scopeMap["this"] = anyScopeId
        }
        val rootScope = scopeMap["root"]
        if(rootScope == null || rootScope == unknownScopeId) {
            scopeMap["root"] = anyScopeId
        }
    }
}