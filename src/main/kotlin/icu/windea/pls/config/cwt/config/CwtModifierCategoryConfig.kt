package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.script.*
import icu.windea.pls.cwt.psi.*

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string | string[]
 */
data class CwtModifierCategoryConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val internalId: String? = null,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>{
	val supportAnyScope = supportedScopes == ScopeConfigHandler.anyScopeIdSet
}

