package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val subName: String,
	val config: CwtPropertyConfig
) : CwtConfig<CwtProperty> {
	val expression = CwtKeyExpression.resolve(subName)
	val supportedScopes = inferSupportedScopes()
	val supportedScopesText = supportedScopes.joinToString(" ","{ "," }")
	
	private fun inferSupportedScopes(): List<String> {
		val options = config.options ?: return emptyList()
		return options.find { it.key == "scope" || it.key == "scopes" }?.stringValueOrValues ?: emptyList()
	}
}

