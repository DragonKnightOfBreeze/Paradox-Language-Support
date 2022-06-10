package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string[]
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtModifierCategoryConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val internalId: String? = null,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>{
	val supportedScopeNames: MutableSet<String> = mutableSetOf()
}

