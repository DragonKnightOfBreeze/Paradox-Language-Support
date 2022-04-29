package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property key (property key)
 * @property expression (property value)
 * @property required (option) required
 * @property primary (option) primary
 */
data class CwtLocationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val key: String,
	val expression: String,
	val required: Boolean = false,
	val primary: Boolean = false,
): CwtConfig<CwtProperty>