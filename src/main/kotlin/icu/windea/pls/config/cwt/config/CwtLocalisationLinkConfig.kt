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
	val inputScopes: Set<String>,
	val outputScope: String
) : CwtConfig<CwtProperty> {
	val inputAnyScope get() = inputScopes == ScopeConfigHandler.anyScopeIdSet
	val outputAnyScope get() = outputScope == ScopeConfigHandler.anyScopeId
}
