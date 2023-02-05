package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理作用域。
 */
object ParadoxScopeHandler {
	const val maxScopeLinkSize = 5
	
	const val unknownScopeId = "?"
	const val anyScopeId = "any"
	const val allScopeId = "all"
	
	val anyScopeIdSet = setOf(anyScopeId)
	
	/**
	 * 得到作用域的ID（全小写+下划线）。
	 */
	@JvmStatic
	fun getScopeId(scope: String): String {
		val scopeId = scope.lowercase().replace(' ', '_')
		//"all" scope are always resolved as "any" scope
		if(scopeId == allScopeId) return anyScopeId
		return scopeId
	}
	
	/**
	 * 得到作用域的名字。
	 */
	@JvmStatic
	fun getScopeName(scope: String, configGroup: CwtConfigGroup): String {
		//handle "any" and "all" scope 
		if(scope.equals(anyScopeId, true)) return "Any"
		if(scope.equals(allScopeId, true)) return "All"
		//a scope may not have aliases, or not defined in scopes.cwt
		return configGroup.scopes[scope]?.name
			?: configGroup.scopeAliasMap[scope]?.name
			?: scope.toCapitalizedWords()
	}
	
	@JvmStatic
	fun isFakeScopeId(scopeId: String): Boolean {
		return scopeId == unknownScopeId || scopeId == anyScopeId || scopeId == allScopeId
	}
	
	@JvmStatic
	fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
		val thisScope = scopeContext?.thisScope
		if(thisScope == null) return true
		if(scopeToMatch == anyScopeId) return true
		if(thisScope == anyScopeId) return true
		if(thisScope == unknownScopeId) return true
		if(thisScope == scopeToMatch) return true
		val scopeConfig = configGroup.scopeAliasMap[thisScope]
		if(scopeConfig != null) return scopeConfig.aliases.any { it == scopeToMatch }
		return false
	}
	
	@JvmStatic
	fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
		val thisScope = scopeContext?.thisScope
		if(thisScope == null) return true
		if(scopesToMatch == null || scopesToMatch.isEmpty() || scopesToMatch == anyScopeIdSet) return true
		if(thisScope == anyScopeId) return true
		if(thisScope == unknownScopeId) return true
		if(thisScope in scopesToMatch) return true
		val scopeConfig = configGroup.scopeAliasMap[thisScope]
		if(scopeConfig != null) return scopeConfig.aliases.any { it in scopesToMatch }
		return false
	}
	
	@JvmStatic
	fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
		val thisScope = scopeContext?.thisScope
		if(thisScope == null) return true
		if(thisScope == anyScopeId) return true
		if(thisScope == unknownScopeId) return true
		val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch]
		if(scopeGroupConfig != null) return scopeGroupConfig.values.any { thisScope == it }
		return false //cwt config error
	}
	
	@JvmStatic
	fun findParentMember(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
		return element.parents(withSelf = false)
			.find { it is ParadoxScriptDefinitionElement || (it is ParadoxScriptBlock && it.isBlockValue()) }
			.castOrNull<ParadoxScriptMemberElement>()
	}
	
	@JvmStatic
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
		val configs = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
		return configs.any { config ->
			val configGroup = config.info.configGroup
			isScopeContextSupportedAsRoot(config, configGroup) || isScopeContextSupportedAsChild(config, configGroup)
		}
	}
	
	private fun isScopeContextSupportedAsRoot(config: CwtDataConfig<*>, configGroup: CwtConfigGroup): Boolean {
		if(config !is CwtPropertyConfig) return false
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
			}
			currentConfig = currentConfig.parent ?: break
		}
		return false
	}
	
	@JvmStatic
	fun isScopeContextChanged(element: ParadoxScriptMemberElement, scopeContext: ParadoxScopeContext): Boolean {
		//does not have scope context > changed always
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
	@JvmStatic
	fun getScopeContext(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
			val file = element.containingFile
			val value = resolveScopeContextOfDefinitionMember(element)
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	private fun resolveScopeContextOfDefinitionMember(element: ParadoxScriptMemberElement): ParadoxScopeContext? {
		//should be a definition
		val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
		if(definitionInfo != null) {
			val configGroup = definitionInfo.configGroup
			//on_action
			if(definitionInfo.type == "on_action") {
				val scopeContext = configGroup.onActions[definitionInfo.name]?.scopeContext
				if(scopeContext != null) return scopeContext
			}
			val declarationConfig = definitionInfo.declarationConfig?.propertyConfig ?: return null
			val subtypeConfigs = definitionInfo.subtypeConfigs
			val typeConfig = definitionInfo.typeConfig
			val replaceScopeOnType = subtypeConfigs.firstNotNullOfOrNull { it.config.replaceScope }
				?: typeConfig.config.replaceScope
			val replaceScope = replaceScopeOnType
				?: declarationConfig.replaceScope
			val pushScopeOnType = (subtypeConfigs.firstNotNullOfOrNull { it.config.pushScope }
				?: typeConfig.config.pushScope)
			val pushScope = pushScopeOnType
				?: declarationConfig.pushScope
			val result = replaceScope?.resolve(pushScope)
				?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
				?: resolveAnyScopeContext()
			return result
		}
		
		//should be a definition member
		val parentMember = findParentMember(element) ?: return null
		val parentScopeContext = getScopeContext(parentMember)
		val configs = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
		val config = configs.firstOrNull()
		if(config == null) return null
		if(config is CwtPropertyConfig && config.expression.type == CwtDataType.ScopeField) {
			if(parentScopeContext == null) return null
			val scopeField = element.castOrNull<ParadoxScriptProperty>()?.propertyKey?.text ?: return null
			if(scopeField.isLeftQuoted()) return null
			val textRange = TextRange.create(0, scopeField.length)
			val configGroup = config.info.configGroup
			val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup, true) ?: return null
			val result = resolveScopeContext(scopeFieldExpression, parentScopeContext)
			return result
		} else {
			val resolvedConfig = config.resolved()
			val replaceScope = resolvedConfig.replaceScope ?: parentScopeContext ?: return null
			val pushScope = resolvedConfig.pushScope
			val result = replaceScope.resolve(pushScope)
			return result
		}
	}
	
	@JvmStatic
	fun getScopeContext(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
			val file = element.containingFile
			val value = resolveScopeContextOfLocalisationCommandIdentifier(element)
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	private fun resolveScopeContextOfLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext {
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
								return resolveAnyScopeContext()
							}
							return ParadoxScopeContext.resolve(config.outputScope)
						}
						return prevScopeContext.resolve(config.outputScope)
					}
					is CwtSystemLinkConfig -> {
						return resolveAnyScopeContext()
					}
					//predefined event target - no scope info in cwt files yet
					is CwtValueConfig -> {
						return resolveAnyScopeContext()
					}
				}
			}
			//TODO event target or global event target - not supported yet
			prevResolved is ParadoxValueSetValueElement -> {
				return resolveAnyScopeContext()
			}
		}
		return resolveUnknownScopeContext()
	}
	
	@JvmStatic
	fun resolveScopeContext(systemLink: CwtSystemLinkConfig, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		return resolveScopeContextBySystemLink(systemLink, inputScopeContext)
	}
	
	@JvmStatic
	fun resolveScopeContext(scopeFieldExpression: ParadoxScopeFieldExpression, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
		val scopeNodes = scopeFieldExpression.scopeNodes
		var result = inputScopeContext
		val resolved = mutableListOf<Tuple2<ParadoxScopeExpressionNode, ParadoxScopeContext>>()
		for(scopeNode in scopeNodes) {
			result = resolveScopeContext(scopeNode, result)
			resolved.add(scopeNode to result)
			if(scopeNode is ParadoxErrorScopeExpressionNode) break
		}
		result.scopeFieldInfo = resolved
		return result
	}
	
	@JvmStatic
	fun resolveScopeContext(scopeNode: ParadoxScopeExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
		return when(scopeNode) {
			is ParadoxScopeLinkExpressionNode -> {
				resolveScopeByScopeLinkNode(scopeNode, inputScopeContext)
			}
			is ParadoxScopeLinkFromDataExpressionNode -> {
				resolveScopeByScopeLinkFromDataNode(scopeNode, inputScopeContext)
			}
			is ParadoxSystemLinkExpressionNode -> {
				resolveScopeContextBySystemLinkNode(scopeNode, inputScopeContext)
					?: resolveUnknownScopeContext(inputScopeContext)
			}
			//error
			is ParadoxErrorScopeExpressionNode -> resolveUnknownScopeContext(inputScopeContext)
		}
	}
	
	@JvmStatic
	fun resolveScopeContext(node: ParadoxLinkPrefixExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
		val linkConfig = node.linkConfigs.firstOrNull() // first is ok
		if(linkConfig == null) return inputScopeContext //unexpected
		return inputScopeContext.resolve(linkConfig.outputScope)
	}
	
	private fun resolveScopeByScopeLinkNode(node: ParadoxScopeLinkExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
		val outputScope = node.config.outputScope
		return inputScopeContext.resolve(outputScope)
	}
	
	private fun resolveScopeByScopeLinkFromDataNode(node: ParadoxScopeLinkFromDataExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext {
		val linkConfig = node.linkConfigs.firstOrNull() // first is ok
		if(linkConfig == null) return inputScopeContext //unexpected
		return inputScopeContext.resolve(linkConfig.outputScope)
	}
	
	private fun resolveScopeContextBySystemLinkNode(node: ParadoxSystemLinkExpressionNode, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		val systemLinkConfig = node.config
		return resolveScopeContextBySystemLink(systemLinkConfig, inputScopeContext)
	}
	
	private fun resolveScopeContextBySystemLink(systemLink: CwtSystemLinkConfig, inputScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		val id = systemLink.id
		val systemLinkContext = when {
			id == "This" -> inputScopeContext
			id == "Root" -> inputScopeContext.root
			id == "Prev" -> inputScopeContext.prev
			id == "PrevPrev" -> inputScopeContext.prev?.prev
			id == "PrevPrevPrev" -> inputScopeContext.prev?.prev?.prev
			id == "PrevPrevPrevPrev" -> inputScopeContext.prev?.prev?.prev?.prev
			id == "From" -> inputScopeContext.from
			id == "FromFrom" -> inputScopeContext.from?.from
			id == "FromFromFrom" -> inputScopeContext.from?.from?.from
			id == "FromFromFromFrom" -> inputScopeContext.from?.from?.from
			else -> null
		} ?: return null
		return inputScopeContext.resolve(systemLinkContext)
	}
	
	@JvmStatic
	fun resolveAnyScopeContext(): ParadoxScopeContext {
		return ParadoxScopeContext.resolve(anyScopeId, anyScopeId)
	}
	
	@JvmStatic
	fun resolveUnknownScopeContext(inputScopeContext: ParadoxScopeContext? = null): ParadoxScopeContext {
		return inputScopeContext?.resolve(unknownScopeId) ?: ParadoxScopeContext.resolve(unknownScopeId)
	}
}