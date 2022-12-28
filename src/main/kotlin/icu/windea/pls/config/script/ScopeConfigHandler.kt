package icu.windea.pls.config.script

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理作用域。
 */
object ScopeConfigHandler {
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
	fun matchesScope(thisScope: String?, scopes: Set<String>?): Boolean {
		if(thisScope == null) return true
		if(scopes == null || scopes.isEmpty() || scopes == anyScopeIdSet) return true
		if(thisScope == anyScopeId) return true
		if(thisScope == unknownScopeId) return true
		return thisScope in scopes
	}
	
	@JvmStatic
	fun matchesScope(scopeContext: ParadoxScopeContext?, scopes: Set<String>?): Boolean {
		val thisScope = scopeContext?.thisScope
		return matchesScope(thisScope, scopes)
	}
	
	//fun matchScope(scopes: Collection<String>?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
	//	if(scopes.isNullOrEmpty()) return true
	//	return scopes.any { scope -> matchScope(scope, scopesToMatch, configGroup) }
	//}
	
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
			aliasName != null && aliasName in configGroup.aliasNameSupportScope
		}
	}
	
	private fun isScopeContextSupportedAsChild(config: CwtDataConfig<*>, configGroup: CwtConfigGroup): Boolean {
		var currentConfig = config
		while(true) {
			if(currentConfig is CwtPropertyConfig) {
				val inlineableConfig = currentConfig.inlineableConfig
				if(inlineableConfig is CwtAliasConfig) {
					val aliasName = inlineableConfig.name
					if(aliasName in configGroup.aliasNameSupportScope) return true
				}
			}
			currentConfig = currentConfig.parent ?: break
		}
		return false
	}
	
	@JvmStatic
	fun isScopeContextChanged(element: ParadoxScriptMemberElement, scopeContext: ParadoxScopeContext, file: PsiFile = element.containingFile): Boolean {
		//does not have scope context > changed always
		val parentMember = findParentMember(element)
		if(parentMember == null) return true
		val parentScopeContext = getScopeContext(parentMember, file)
		if(parentScopeContext == null) return true
		if(parentScopeContext != scopeContext) return true
		if(!isScopeContextSupported(parentMember)) return true
		return false
	}
	
	@JvmStatic
	fun getScopeContext(element: ParadoxScriptMemberElement, file: PsiFile = element.containingFile): ParadoxScopeContext? {
		return getScopeContextFromCache(element, file)
	}
	
	private fun getScopeContextFromCache(element: ParadoxScriptMemberElement, file: PsiFile): ParadoxScopeContext? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
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
			return replaceScope?.resolve(pushScope)
				?: ParadoxScopeContext.resolve(anyScopeId, anyScopeId)
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
			return resolveScopeContextFromScopeField(scopeField, config, parentScopeContext)
				?: resolveUnknownScopeContext(parentScopeContext)
		} else {
			val resolvedConfig = config.resolved()
			val replaceScope = resolvedConfig.replaceScope ?: parentScopeContext
			val pushScope = resolvedConfig.pushScope
			return replaceScope?.resolve(pushScope)
		}
	}
	
	private fun resolveScopeContextFromScopeField(text: String, config: CwtPropertyConfig, parentScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		if(text.isLeftQuoted()) return null
		val textRange = TextRange.create(0, text.length)
		val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, config.info.configGroup, true) ?: return null
		return resolveScopeContextFromScopeFieldExpression(scopeFieldExpression, parentScopeContext)
	}
	
	private fun resolveScopeContextFromScopeFieldExpression(scopeFieldExpression: ParadoxScopeFieldExpression, parentScopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		val scopeNodes = scopeFieldExpression.scopeNodes
		var resolvedScope: String? = null
		var scopeContext = parentScopeContext
		for(scopeNode in scopeNodes) {
			when(scopeNode) {
				is ParadoxScopeLinkExpressionNode -> {
					resolvedScope = resolveScopeByScopeLink(scopeNode)
					break
				}
				is ParadoxScopeLinkFromDataExpressionNode -> {
					resolvedScope = resolveScopeByScopeLinkFromData(scopeNode)
					break
				}
				is ParadoxSystemScopeExpressionNode -> {
					scopeContext = resolveScopeBySystemScope(scopeNode, scopeContext) ?: return null
					resolvedScope = scopeContext.thisScope
				}
				is ParadoxErrorScopeExpressionNode -> {
					return null //error
				}
			}
		}
		if(resolvedScope == null) return null
		return scopeContext.resolve(resolvedScope)
	}
	
	private fun resolveScopeByScopeLink(node: ParadoxScopeLinkExpressionNode): String {
		return node.config.outputScope
	}
	
	private fun resolveScopeByScopeLinkFromData(node: ParadoxScopeLinkFromDataExpressionNode): String? {
		return null //TODO
	}
	
	private fun resolveScopeBySystemScope(node: ParadoxSystemScopeExpressionNode, scopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		val systemScopeConfig = node.config
		return resolveScopeBySystemScope(systemScopeConfig, scopeContext)
	}
	
	@JvmStatic
	fun getScopeContext(element: ParadoxLocalisationCommandIdentifier, file: PsiFile = element.containingFile): ParadoxScopeContext? {
		return getScopeContextFromCache(element, file)
	}
	
	private fun getScopeContextFromCache(element: ParadoxLocalisationCommandIdentifier, file: PsiFile): ParadoxScopeContext? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
			val value = resolveScopeContextOfLocalisationCommandIdentifier(element)
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	private fun resolveScopeContextOfLocalisationCommandIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxScopeContext? {
		//TODO depends on usages
		val prevElement = element.prevIdentifier
		val prevResolved = prevElement?.reference?.resolve()
		when {
			//system scope or localisation scope
			prevResolved is CwtProperty -> {
				val config = prevResolved.getUserData(PlsKeys.cwtConfigKey)
				when {
					config is CwtLocalisationLinkConfig -> {
						val prevPrevElement = prevElement.prevIdentifier
						val prevScopeContext = if(prevPrevElement != null) getScopeContext(prevPrevElement) else null
						return prevScopeContext?.resolve(config.outputScope)
							?: ParadoxScopeContext.resolve(config.outputScope)
					}
					config is CwtSystemScopeConfig -> {
						return resolveUnknownScopeContext()
					}
				}
			}
			//TODO event target or global event target - not supported yet
			prevResolved is ParadoxValueSetValueElement -> {
				return resolveUnknownScopeContext()
			}
		}
		return null
	}
	
	@JvmStatic
	fun resolveUnknownScopeContext(parentScopeContext: ParadoxScopeContext? = null): ParadoxScopeContext {
		return parentScopeContext?.resolve(unknownScopeId) ?: ParadoxScopeContext.resolve(unknownScopeId)
	}
	
	@JvmStatic
	fun resolveScopeBySystemScope(systemScope: CwtSystemScopeConfig, scopeContext: ParadoxScopeContext): ParadoxScopeContext? {
		val id = systemScope.id
		return when {
			id == "This" -> scopeContext
			id == "Root" -> scopeContext.root
			id == "Prev" -> scopeContext.prev
			id == "PrevPrev" -> scopeContext.prev?.prev
			id == "PrevPrevPrev" -> scopeContext.prev?.prev?.prev
			id == "PrevPrevPrevPrev" -> scopeContext.prev?.prev?.prev?.prev
			id == "From" -> scopeContext.from
			id == "FromFrom" -> scopeContext.from?.from
			id == "FromFromFrom" -> scopeContext.from?.from?.from
			id == "FromFromFromFrom" -> scopeContext.from?.from?.from
			else -> null
		}
	}
}