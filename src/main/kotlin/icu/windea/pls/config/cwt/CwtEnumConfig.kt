package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtEnumConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val values:List<String>,
	val valueConfigs:List<CwtValueConfig>
): CwtConfig<CwtProperty>

