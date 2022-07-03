package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val name: String,
	val subName: String,
	override val config: CwtPropertyConfig
) : CwtInlineableConfig {
	val keyExpression by lazy { CwtKeyExpression.resolve(subName) }
	val valueExpression by lazy { CwtValueExpression.resolve(subName) }
	
	//TODO check
	
	val supportedScopes by lazy { resolveSupportedScopes() }
	val supportedScopesText by lazy { supportedScopes?.joinToString(" ", "{ ", " }") }
	
	private fun resolveSupportedScopes(): Set<String>? {
		val options = config.options ?: return emptySet()
		val option = options.find { it.key == "scope" || it.key == "scopes" } ?: return null
		return option.stringValue?.let { setOf(it) } ?: option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } ?: emptySet()
	}
}