package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtLocalisationCommandConfig(
	val name:String,
	val values:List<String>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
):CwtConfig