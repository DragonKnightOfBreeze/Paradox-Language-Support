package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
data class CwtScopeConfig(
	val name:String,
	val aliases:List<String>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
):CwtConfig
