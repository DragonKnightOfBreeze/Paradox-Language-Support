package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string[]
 */
data class CwtModifyCategoryConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val internalId: Int,
	val supportedScopes:List<String>
): CwtConfig<CwtProperty>