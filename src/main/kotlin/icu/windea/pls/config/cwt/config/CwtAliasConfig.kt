package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

/**
 *
 * @property supportedScopes (option) scope/scopes: string | string[]
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	override val config: CwtPropertyConfig,
	override val name: String,
	val subName: String
) : CwtInlineableConfig {
	val keyExpression = CwtKeyExpression.resolve(subName)
	val valueExpression = CwtValueExpression.resolve(subName)
	
	//TODO check
	
	val supportedScopes = config.options
		?.find { o -> o.key == "scope" || o.key == "scopes" }
		?.let { o -> o.stringValue?.let { setOf(it) } ?: o.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } }
	val supportAnyScope = supportedScopes.isNullOrEmpty() || supportedScopes.singleOrNull() == "any"
	val supportedScopeNames by lazy {
		if(supportAnyScope) {
			setOf("Any")
		} else {
			supportedScopes?.mapTo(mutableSetOf()) { CwtConfigHandler.getScopeName(it, info.configGroup) }.orEmpty()
		}
	}
}