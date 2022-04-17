package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
data class CwtScopeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val aliases:List<String>
): CwtConfig<CwtProperty>
