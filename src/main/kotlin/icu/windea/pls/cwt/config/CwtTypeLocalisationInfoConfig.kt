package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property required (option) required
 * @property primary (option) primary
 */
data class CwtTypeLocalisationInfoConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val expression: String,
	val required: Boolean = false,
	val primary: Boolean = false,
): CwtConfig<CwtProperty>