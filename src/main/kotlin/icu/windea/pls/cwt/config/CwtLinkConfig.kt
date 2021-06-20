package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property inputScopes input_scopes | inputscopes: string[]
 * @property outputScope output_scope: string
 */
data class CwtLinkConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val inputScopes:List<String>,
	val outputScope:String,
): CwtConfig<CwtProperty>

