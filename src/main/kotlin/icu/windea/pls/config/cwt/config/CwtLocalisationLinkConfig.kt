package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.script.*
import icu.windea.pls.cwt.psi.*

/**
 * @property desc desc: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string
 */
data class CwtLocalisationLinkConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String,
	val desc: String? = null,
	val inputScopes: Set<String>?,
	val outputScope: String? = null
) : CwtConfig<CwtProperty> {
	val inputAnyScope = inputScopes.isNullOrEmpty() || inputScopes.singleOrNull().let { it == "any" || it == "all" }
	val outputAnyScope = outputScope == null || outputScope == "any"
	
	val inputScopeNames by lazy {
		if(inputAnyScope) {
			setOf("Any")
		} else {
			inputScopes?.mapTo(mutableSetOf()) { ScopeConfigHandler.getScopeName(it, info.configGroup) }.orEmpty()
		}
	}
	val outputScopeName by lazy {
		if(outputAnyScope) {
			"Any"
		} else {
			ScopeConfigHandler.getScopeName(outputScope ?: "any", info.configGroup)
		}
	}
}
