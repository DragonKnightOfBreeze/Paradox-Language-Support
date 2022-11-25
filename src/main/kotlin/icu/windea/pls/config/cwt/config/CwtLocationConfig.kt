package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property key (property key)
 * @property value (property value)
 * @property required (option) required
 * @property primary (option) primary
 */
data class CwtLocationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val key: String,
	val value: String,
	val required: Boolean = false,
	val primary: Boolean = false,
): CwtConfig<CwtProperty>
