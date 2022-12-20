package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.cwt.psi.*

/**
 *
 * @property supportedScopes (option) scope/scopes: string | string[]
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	override val config: CwtPropertyConfig,
	override val name: String,
	val subName: String
) : CwtInlineableConfig {
	val subNameExpression = CwtKeyExpression.resolve(subName).registerTo(info)
	
	override val expression get() = subNameExpression
	
	//TODO check
	
	val supportedScopes = config.options
		?.find { o -> o.key == "scope" || o.key == "scopes" }
		?.let { o -> o.stringValue?.let { setOf(it) } ?: o.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } }
	val supportAnyScope = supportedScopes.isNullOrEmpty() || supportedScopes.singleOrNull() == "any"
	val supportedScopeNames by lazy {
		if(supportAnyScope) {
			setOf("Any")
		} else {
			supportedScopes?.mapTo(mutableSetOf()) { ScopeConfigHandler.getScopeName(it, info.configGroup) }.orEmpty()
		}
	}
}
