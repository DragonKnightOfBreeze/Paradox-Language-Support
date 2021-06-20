package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtEnumConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val values:List<String>
): icu.windea.pls.cwt.config.CwtConfig<CwtProperty>

