package icu.windea.pls.config.definition

import com.intellij.psi.PsiFile
import com.intellij.psi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理作用域。
 */
object ScopeConfigHandler {
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
	
	fun isScopeContextEntry(element: ParadoxScriptMemberElement) :Boolean {
		//用于过滤在那些地方（所在行的末尾）显示作用域上下文的内嵌提示
		return element is ParadoxScriptProperty && element.propertyValue is ParadoxScriptBlock
	}
	
	fun getScopeContext(element: ParadoxScriptMemberElement, file: PsiFile = element.containingFile) : ParadoxScopeConfig? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedScopeContextKey) {
			val value = resolveScopeContext(element)
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	private fun resolveScopeContext(element: ParadoxScriptMemberElement): ParadoxScopeConfig? {
		val parentMember = element.parentOfType<ParadoxScriptMemberElement>()
		if(parentMember == null) {
			//should be a definition
			val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
			if(definitionInfo == null) return null
			val declarationConfig = definitionInfo.declarationConfig?.propertyConfig ?: return null
			val subtypeConfigs = definitionInfo.subtypeConfigs
			val typeConfig = definitionInfo.typeConfig
			val replaceScope = subtypeConfigs.firstNotNullOfOrNull { it.config.replaceScope }
				?: typeConfig.config.replaceScope
				?: declarationConfig.replaceScope
			val pushScope = subtypeConfigs.firstNotNullOfOrNull { it.config.pushScope } 
				?: typeConfig.config.pushScope
				?: declarationConfig.pushScope
			return replaceScope?.resolve(pushScope)
		} else {
			//should be a definition member
			val parentScopeContext = ScopeConfigHandler.getScopeContext(parentMember)
			val configs = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
			val config = configs.firstOrNull()
			if(config == null) return null
			val replaceScope = config.replaceScope ?: parentScopeContext
			val pushScope = config.pushScope
			return replaceScope?.resolveNew(pushScope)
		}
	}
}