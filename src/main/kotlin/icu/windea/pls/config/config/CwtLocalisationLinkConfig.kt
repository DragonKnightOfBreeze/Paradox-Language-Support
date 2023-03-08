package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property desc desc: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string? - 为null时表示会传递scope
 */
data class CwtLocalisationLinkConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String,
	val desc: String? = null,
	val inputScopes: Set<String>,
	val outputScope: String?
) : CwtConfig<CwtProperty>
