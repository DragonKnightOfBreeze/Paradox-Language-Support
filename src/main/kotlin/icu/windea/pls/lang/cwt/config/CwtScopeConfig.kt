package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
class CwtScopeConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val aliases: Set<@CaseInsensitive String>
) : CwtConfig<CwtProperty>
