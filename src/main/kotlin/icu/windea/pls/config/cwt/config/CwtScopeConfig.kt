package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
data class CwtScopeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val aliases: Set<String>
) : CwtConfig<CwtProperty>
