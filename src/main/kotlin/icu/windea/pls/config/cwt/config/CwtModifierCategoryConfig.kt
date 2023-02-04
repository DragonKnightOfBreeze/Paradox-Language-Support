package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

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
	val supportAnyScope get() = supportedScopes == ParadoxScopeHandler.anyScopeIdSet
}

fun Map<String, CwtModifierCategoryConfig>.getSupportedScopes(): Set<String> {
	val categoryConfigs = this.values
	if(categoryConfigs.any { it.supportAnyScope }) {
		return ParadoxScopeHandler.anyScopeIdSet
	} else {
		return categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
	}
}