package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val subName: String,
	val config: CwtPropertyConfig
) : CwtConfig<CwtProperty> {
	val expression by lazy { CwtKeyExpression.resolve(subName) }
	
	val supportedScopes by lazy { inferSupportedScopes() }
	val supportedScopesText by lazy { supportedScopes.joinToString(" ", "{ ", " }") }
	
	private fun inferSupportedScopes(): List<String> {
		val options = config.options ?: return emptyList()
		return options.find { it.key == "scope" || it.key == "scopes" }?.stringValueOrValues ?: emptyList()
	}
}

