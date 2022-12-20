package icu.windea.pls.config.definition

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理作用域。
 */
object ScopeConfigHandler {
	const val unknownScopeId = "<unknown>"
	const val anyScopeId = "any"
	const val allScopeId = "all"
	
	/**
	 * 得到作用域的名字（用于显示在快速文档中）。
	 */
	@JvmStatic
	fun getScopeName(scopeId: String, configGroup: CwtConfigGroup): String {
		//handle "any" and "all" scope 
		if(scopeId.equals("any", true)) return "Any"
		if(scopeId.equals("all", true)) return "All"
		//a scope may not have aliases, or not defined in scopes.cwt
		return configGroup.scopes[scopeId]?.name
			?: configGroup.scopeAliasMap[scopeId]?.name
			?: scopeId.toCapitalizedWords()
	}
	
	fun matchesScope(scopeId: String?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
		if(scopeId == null || scopeId.equals("any", true) || scopeId.equals("all", true)) return true
		if(scopesToMatch.isNullOrEmpty()) return true
		return scopesToMatch.any { s ->
			if(s.equals("any", true) || s.equals("all", true)) return@any true
			scopeId.equals(s, true) || configGroup.scopeAliasMap[scopeId]?.aliases?.contains(s) == true
		}
	}
	
	//fun matchScope(scopes: Collection<String>?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
	//	if(scopes.isNullOrEmpty()) return true
	//	return scopes.any { scope -> matchScope(scope, scopesToMatch, configGroup) }
	//}
	
	//fun isScopeContextEntry(element: ParadoxScriptMemberElement) :Boolean {
	//	//用于过滤在那些地方（所在行的末尾）显示作用域上下文的内嵌提示
	//	return element is ParadoxScriptProperty && element.propertyValue is ParadoxScriptBlock
	//}
	
	fun getScopeContext(element: ParadoxScriptMemberElement, file: PsiFile = element.containingFile) : ParadoxScopeConfig? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
			val value = resolveScopeContext(element)
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	private fun resolveScopeContext(element: ParadoxScriptMemberElement): ParadoxScopeConfig? {
		//should be a definition
		val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
		if(definitionInfo != null) {
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
			return replaceScope?.resolve(pushScope)?.apply {
				this.fromTypeConfig = replaceScopeOnType != null || pushScopeOnType != null
			}
		}
		
		//should be a definition member
		val parentMember = findParentMember(element) ?: return null
		val parentScopeContext = getScopeContext(parentMember)
		val configs = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
		val config = configs.firstOrNull()
		if(config == null) return null
		if(config is CwtPropertyConfig && config.expression.type == CwtDataTypes.ScopeField) {
			if(parentScopeContext == null) return null
			return resolveScopeContextFromScopeField(element, config, parentScopeContext)
				?: resolveUnknownScopeContext(parentScopeContext)
		}
		val replaceScope = config.replaceScope ?: parentScopeContext
		val pushScope = config.pushScope
		return replaceScope?.resolve(pushScope)
	}
	
	private fun findParentMember(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
		return element.parents(withSelf = false)
			.find { it is ParadoxScriptDefinitionElement || (it is ParadoxScriptBlock && it.isBlockValue()) }
			.castOrNull<ParadoxScriptMemberElement>()
	}
	
	private fun resolveUnknownScopeContext(parentScopeContext: ParadoxScopeConfig): ParadoxScopeConfig {
		return parentScopeContext.resolve(unknownScopeId)
	}
	
	private fun resolveScopeContextFromScopeField(element: ParadoxScriptMemberElement, config: CwtPropertyConfig, parentScopeContext: ParadoxScopeConfig): ParadoxScopeConfig? {
		val text = element.text
		if(text.isLeftQuoted()) return null
		val textRange = TextRange.create(0, text.length)
		val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, config.info.configGroup, true) ?: return null
		return resolveScopeContextFromScopeFieldExpression(scopeFieldExpression, parentScopeContext)
	}
	
	private fun resolveScopeContextFromScopeFieldExpression(scopeFieldExpression: ParadoxScopeFieldExpression, parentScopeContext: ParadoxScopeConfig): ParadoxScopeConfig? {
		val scopeNodes = scopeFieldExpression.scopeNodes
		var resolvedScope: String? = null
		var scopeContext = parentScopeContext
		for(i in scopeNodes.lastIndex .. 0) {
			val scopeNode = scopeNodes[i]
			when(scopeNode) {
				is ParadoxScopeLinkExpressionNode -> {
					resolvedScope = resolveScopeFromScopeLink(scopeNode)
					break
				}
				is ParadoxScopeLinkFromDataExpressionNode -> {
					resolvedScope = resolveScopeFromScopeLinkFromData(scopeNode)
					break
				}
				is ParadoxSystemScopeExpressionNode -> {
					scopeContext = resolveScopeFromSystemScope(scopeNode, scopeContext) ?: return null
					resolvedScope = scopeContext.thisScope
				}
				is ParadoxErrorScopeExpressionNode -> {
					return null //error
				}
			}
		}
		if(resolvedScope == null) return null
		return parentScopeContext.resolve(resolvedScope)
	}
	
	private fun resolveScopeFromScopeLink(node: ParadoxScopeLinkExpressionNode): String {
		return node.config.outputScope ?: anyScopeId
	}
	
	private fun resolveScopeFromScopeLinkFromData(node: ParadoxScopeLinkFromDataExpressionNode) : String? {
		return null //TODO
	}
	
	private fun resolveScopeFromSystemScope(node: ParadoxSystemScopeExpressionNode, scopeContext: ParadoxScopeConfig) : ParadoxScopeConfig? {
		val systemScopeConfig = node.config
		val id = systemScopeConfig.id
		return when {
			id == "This" -> scopeContext
			id == "Root" -> scopeContext.root
			id == "Prev" -> scopeContext.prev
			id == "PrevPrev" -> scopeContext.prev?.prev
			id == "PrevPrevPrev" -> scopeContext.prev?.prev?.prev
			id == "PrevPrevPrevPrev" -> scopeContext.prev?.prev?.prev?.prev
			id == "From" -> scopeContext.from
			else -> null //TODO
		}
	}
}