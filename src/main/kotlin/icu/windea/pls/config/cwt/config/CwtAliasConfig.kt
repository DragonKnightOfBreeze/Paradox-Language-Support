package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val subName: String,
	val config: CwtPropertyConfig
) : CwtConfig<CwtProperty> {
	val keyExpression by lazy { CwtKeyExpression.resolve(subName) }
	val valueExpression by lazy { CwtValueExpression.resolve(subName) }
	
	val supportedScopes by lazy { inferSupportedScopes() }
	val supportedScopesText by lazy { supportedScopes.joinToString(" ", "{ ", " }") }
	
	private fun inferSupportedScopes(): Set<String> {
		val options = config.options ?: return emptySet()
		val option = options.find { it.key == "scope" || it.key == "scopes" } ?: return emptySet()
		return option.stringValue?.let { setOf(it) } ?: option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } ?: emptySet()
	}
}

