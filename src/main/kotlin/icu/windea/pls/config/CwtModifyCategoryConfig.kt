package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property internalId internal_id: int
 * @property supportedScopes supported_scopes: string[]
 */
data class CwtModifyCategoryConfig(
	val name:String,
	val internalId: Int,
	val supportedScopes:List<String>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
):CwtConfig