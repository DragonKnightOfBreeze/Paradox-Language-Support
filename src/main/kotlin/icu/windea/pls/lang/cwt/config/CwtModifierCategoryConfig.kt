package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string | string[]
 */
class CwtModifierCategoryConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val internalId: String? = null,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>

fun Map<String, CwtModifierCategoryConfig>.getSupportedScopes(): Set<String> {
	val categoryConfigs = this.values
	if(categoryConfigs.any { it.supportedScopes == ParadoxScopeHandler.anyScopeIdSet }) {
		return ParadoxScopeHandler.anyScopeIdSet
	} else {
		return categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopes }
	}
}