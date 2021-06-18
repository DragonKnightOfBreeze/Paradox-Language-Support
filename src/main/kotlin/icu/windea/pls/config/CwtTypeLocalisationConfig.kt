package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property required (option) required
 * @property primary (option) primary
 */
data class CwtTypeLocalisationConfig(
	val name: String,
	val expression: String,
	val required: Boolean = false,
	val primary: Boolean = false,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
):CwtConfig

