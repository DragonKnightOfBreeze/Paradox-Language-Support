package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
data class CwtScopeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val aliases: Set<@CaseInsensitive String>
) : CwtConfig<CwtProperty>
