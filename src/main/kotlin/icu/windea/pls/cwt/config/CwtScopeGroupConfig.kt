package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtScopeGroupConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val values:List<String>
): icu.windea.pls.cwt.config.CwtConfig<CwtProperty>