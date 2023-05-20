package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

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
    
    fun isFakeScopeId(scopeId: String): Boolean {
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
    
    fun isScopeContextSupported(element: ParadoxScriptMemberElement): Boolean {
        //some definitions, such as on_action, also support scope context on definition level
        if(element is ParadoxScriptDefinitionElement) {
            val definitionInfo = element.definitionInfo
            if(definitionInfo != null) {
                val configGroup = definitionInfo.configGroup
                val definitionType = definitionInfo.type
                if(definitionType in configGroup.definitionTypesSupportScope) return true
            }
        }
        
        //child config can be "alias_name[X] = ..." and "alias[X:scope_field]" is valid
        //or root config in config tree is "alias[X:xxx] = ..."
        val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
        configs.forEach { config ->
            val configGroup = config.info.configGroup
            if(config.expression.type == CwtDataType.AliasKeysField) return true
            if(isScopeContextSupportedAsRoot(config, configGroup)) return true
            if(isScopeContextSupportedAsChild(config, configGroup)) return true
        }
        
        //if there is an overridden scope context, so do supported
        val scopeContext = getScopeContext(element)
        if(scopeContext?.overriddenProvider != null) return true
        
        return false
    }
    
    private fun isScopeContextSupportedAsRoot(config: CwtDataConfig<*>, configGroup: CwtConfigGroup): Boolean {
        val properties = config.properties ?: return false
        return properties.any {
            val aliasName = when {
                it.keyExpression.type == CwtDataType.AliasName -> it.keyExpression.value
                else -> null
            }
            aliasName != null && aliasName in configGroup.aliasNamesSupportScope
        }
    }
    
    private fun isScopeContextSupportedAsChild(config: CwtDataConfig<*>, configGroup: CwtConfigGroup): Boolean {
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
            currentConfig = currentConfig.parent ?: break
        }
        return false
    }
    
    fun isScopeContextChanged(element: ParadoxScriptMemberElement, scopeContext: ParadoxScopeContext): Boolean {
        //does not have scope context > changed always
        val parentMember = findParentMember(element)
        if(parentMember == null) return true
        val parentScopeContext = getScopeContext(parentMember)
        if(parentScopeContext == null) return true
        if(!parentScopeContext.isEquivalentTo(scopeContext)) return true
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
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) p@{
            val file = element.containingFile
            val value = doGetScopeContextOfDefinition(element)
                ?: doGetScopeContextOfDefinitionMember(element)
            val tracker = ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker
            CachedValueProvider.Result.create(value, file, tracker)
        }
    }
    
    private fun doGetScopeContextOfDefinition(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //should be a definition
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            element as ParadoxScriptDefinitionElement
            
            //如果推断得到的作用域上下文是确定的，则优先使用推断得到的
            val inferenceInfo = ParadoxDefinitionInferredScopeContextProvider.getScopeContext(element, definitionInfo)
            if(inferenceInfo != null && !inferenceInfo.hasConflict) return inferenceInfo.scopeContext
            
            //使用提供的作用域上下文
            val providedScopeContext = ParadoxDefinitionScopeContextProvider.getScopeContext(element, definitionInfo)
            if(providedScopeContext != null) return providedScopeContext
            
            return getAnyScopeContext()
        }
        return null
    }
    
    private fun doGetScopeContextOfDefinitionMember(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
        //should be a definition member
        val parentMember = findParentMember(element) ?: return null
        val parentScopeContext = getScopeContext(parentMember)
        val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
        val config = configs.firstOrNull()
        if(config == null) return null
        
        val overriddenScopeContext = ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext(element, config)
        if(overriddenScopeContext != null) return overriddenScopeContext
        
        if(config is CwtPropertyConfig && config.expression.type == CwtDataType.ScopeField) {
            if(parentScopeContext == null) return null
            val scopeField = element.castOrNull<ParadoxScriptProperty>()?.propertyKey?.text ?: return null
            if(scopeField.isLeftQuoted()) return null
            val textRange = TextRange.create(0, scopeField.length)
            val configGroup = config.info.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup, true) ?: return null
            val result = getScopeContext(scopeFieldExpression, parentScopeContext)
            return result
        } else {
            //优先基于内联前的规则，如果没有，再基于内联后的规则
            val scopeContext = config.scopeContext ?: config.resolved().scopeContext ?: parentScopeContext ?: return null
            val pushScope = config.pushScope ?: config.resolved().pushScope
            val result = scopeContext.resolve(pushScope)
            return result
        }
    }
    
    fun getScopeContext(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
        return doGetScopeContextFromCache(element)
    }
    
    private fun doGetScopeContextFromCache(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = doGetScopeContextOfLocalisationCommandIdentifier(element)
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetScopeContextOfLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext {
        //TODO depends on usages
        val prevElement = element.prevIdentifier
        val prevResolved = prevElement?.reference?.resolve()
        when {
            //system link or localisation scope
            prevResolved is CwtProperty -> {
                val config = prevResolved.getUserData(PlsKeys.cwtConfigKey)
                when(config) {
                    is CwtLocalisationLinkConfig -> {
                        val prevPrevElement = prevElement.prevIdentifier
                        val prevScopeContext = if(prevPrevElement != null) getScopeContext(prevPrevElement) else null
                        if(prevScopeContext == null) {
                            if(config.outputScope == null) {
                                return getAnyScopeContext()
                            }
                            return ParadoxScopeContext.resolve(config.outputScope, anyScopeId)
                        }
                        return prevScopeContext.resolve(config.outputScope)
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
            //TODO event target or global event target - not supported yet
            prevResolved is ParadoxValueSetValueElement -> {
                return getAnyScopeContext()
            }
        }
        return getUnknownScopeContext()
    }
    
    fun getScopeContext(scopeFieldExpression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val scopeNodes = scopeFieldExpression.scopeNodes
        var result = inputScopeContext
        val resolved = mutableListOf<Tuple2<ParadoxScopeFieldExpressionNode, ParadoxScopeContext>>()
        for((i, scopeNode) in scopeNodes.withIndex()) {
            val inExpression = i != 0
            result = getScopeContext(scopeNode, result, inExpression)
            resolved.add(scopeNode to result)
            if(scopeNode is ParadoxErrorScopeFieldExpressionNode) break
        }
        result.scopeFieldInfo = resolved
        return result
    }
    
    fun getScopeContext(scopeNode: ParadoxScopeFieldExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        return when(scopeNode) {
            is ParadoxScopeLinkExpressionNode -> {
                doGetScopeByScopeLinkNode(scopeNode, inputScopeContext, inExpression)
            }
            is ParadoxScopeLinkFromDataExpressionNode -> {
                doGetScopeByScopeLinkFromDataNode(scopeNode, inputScopeContext, inExpression)
            }
            is ParadoxSystemLinkExpressionNode -> {
                doGetScopeContextBySystemLinkNode(scopeNode, inputScopeContext, inExpression)
                    ?: getUnknownScopeContext(inputScopeContext, scopeNode.config.baseId.equals("from", true))
            }
            is ParadoxParameterizedScopeFieldExpressionNode -> getAnyScopeContext()
            //error
            is ParadoxErrorScopeFieldExpressionNode -> getUnknownScopeContext(inputScopeContext)
        }
    }
    
    fun getScopeContext(node: ParadoxLinkPrefixExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() // first is ok
        if(linkConfig == null) return inputScopeContext //unexpected
        return inputScopeContext.resolve(linkConfig.outputScope)
    }
    
    private fun doGetScopeByScopeLinkNode(node: ParadoxScopeLinkExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        val outputScope = node.config.outputScope
        return inputScopeContext.resolve(outputScope)
    }
    
    private fun doGetScopeByScopeLinkFromDataNode(node: ParadoxScopeLinkFromDataExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext {
        val linkConfig = node.linkConfigs.firstOrNull() // first is ok
        if(linkConfig == null) return inputScopeContext //unexpected
        if(linkConfig.outputScope == null && linkConfig.expression?.type?.isScopeFieldType() == true) {
            //hidden:owner = {...}
            //hidden:event_target:xxx = {...}
            val nestedNode = node.dataSourceNode.nodes.findIsInstance<ParadoxScopeFieldExpressionNode>()
            if(nestedNode != null) {
                return getScopeContext(nestedNode, inputScopeContext, inExpression)
            }
        }
        return inputScopeContext.resolve(linkConfig.outputScope)
    }
    
    private fun doGetScopeContextBySystemLinkNode(node: ParadoxSystemLinkExpressionNode, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext? {
        val systemLinkConfig = node.config
        return doGetScopeContextBySystemLink(systemLinkConfig, inputScopeContext, inExpression)
    }
    
    private fun doGetScopeContextBySystemLink(systemLink: CwtSystemLinkConfig, inputScopeContext: ParadoxScopeContext, inExpression: Boolean): ParadoxScopeContext? {
        fun ParadoxScopeContext.prev(): ParadoxScopeContext? {
            if(inExpression) return prev
            return scopeFieldInfo?.first()?.second?.prev ?: prev
        }
        
        val id = systemLink.id
        val baseId = systemLink.baseId
        val systemLinkContext = when {
            id == "This" -> inputScopeContext
            id == "Root" -> inputScopeContext.root
            id == "Prev" -> inputScopeContext.prev()
            id == "PrevPrev" -> inputScopeContext.prev()?.prev()
            id == "PrevPrevPrev" -> inputScopeContext.prev()?.prev()?.prev()
            id == "PrevPrevPrevPrev" -> inputScopeContext.prev()?.prev()?.prev()?.prev()
            id == "From" -> inputScopeContext.from
            id == "FromFrom" -> inputScopeContext.from?.from
            id == "FromFromFrom" -> inputScopeContext.from?.from?.from
            id == "FromFromFromFrom" -> inputScopeContext.from?.from?.from?.from
            else -> null
        } ?: return null
        val isFrom = baseId == "From"
        return inputScopeContext.resolve(systemLinkContext, isFrom)
    }
    
    fun getAnyScopeContext(): ParadoxScopeContext {
        return ParadoxScopeContext.resolve(anyScopeId, anyScopeId)
    }
    
    fun getUnknownScopeContext(inputScopeContext: ParadoxScopeContext? = null, from: Boolean = false): ParadoxScopeContext {
        if(inputScopeContext == null) return ParadoxScopeContext.resolve(unknownScopeId)
        val resolved = inputScopeContext.resolve(unknownScopeId)
        if(from) {
            resolved.root = null
            resolved.prev = null
        }
        return resolved
    }
    
    fun buildScopeDoc(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement, builder: StringBuilder) {
        with(builder) {
            when {
                isFakeScopeId(scopeId) -> append(scopeId)
                else -> appendCwtLink("${gameType.linkToken}scopes/$scopeId", scopeId, contextElement)
            }
        }
    }
    
    fun buildScopeContextDoc(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement, builder: StringBuilder) {
        with(builder) {
            var appendSeparator = false
            scopeContext.detailMap.forEach { (systemLink, scope) ->
                if(appendSeparator) appendBr() else appendSeparator = true
                appendCwtLink("${gameType.linkToken}system_links/$systemLink", systemLink, contextElement)
                append(" = ")
                when {
                    isFakeScopeId(scope.id) -> append(scope)
                    scope is ParadoxScope.InferredScope -> appendCwtLink("${gameType.linkToken}scopes/${scope.id}", scope.id, contextElement).append("!")
                    else -> appendCwtLink("${gameType.linkToken}scopes/${scope.id}", scope.id, contextElement)
                }
            }
        }
    }
    
    fun mergeScopeContextMap(map: Map<String, String?>, otherMap: Map<String, String?>): Map<String, String?>? {
        val result = mutableMapOf<String, String?>()
        doMergeScopeContextMap(result, map, otherMap, "this", true).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "root", true).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "prev", false).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "from", false).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "fromfrom", false).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "fromfromfrom", false).let { if(!it) return null }
        doMergeScopeContextMap(result, map, otherMap, "fromfromfromfrom", false).let { if(!it) return null }
        return result
    }
    
    private fun doMergeScopeContextMap(result: MutableMap<String, String?>, m1: Map<String, String?>, m2: Map<String, String?>, key: String, orUnknown: Boolean): Boolean {
        val r = mergeScopeId(m1[key], m2[key])
        result[key] = if(orUnknown) r else r.takeUnless { it == unknownScopeId }
        return r != null
    }
    
    fun mergeScopeId(scopeId: String?, otherScopeId: String?): String? {
        if(scopeId == anyScopeId || otherScopeId == anyScopeId) return anyScopeId
        if(scopeId == unknownScopeId || otherScopeId == unknownScopeId) return unknownScopeId
        if(scopeId == null || otherScopeId == null) return unknownScopeId
        if(scopeId == otherScopeId) return scopeId
        return null
    }
    
    fun mergeScope(scope: ParadoxScope?, otherScope: ParadoxScope?): ParadoxScope? {
        if(scope == ParadoxScope.AnyScope || otherScope == ParadoxScope.AnyScope) return ParadoxScope.AnyScope
        if(scope == ParadoxScope.UnknownScope || otherScope == ParadoxScope.UnknownScope) return ParadoxScope.UnknownScope
        if(scope == null || otherScope == null) return ParadoxScope.UnknownScope
        if(scope.id == otherScope.id) {
            return when {
                scope is ParadoxScope.Scope -> scope
                otherScope is ParadoxScope.Scope -> otherScope
                else -> scope
            }
        }
        return null
    }
}