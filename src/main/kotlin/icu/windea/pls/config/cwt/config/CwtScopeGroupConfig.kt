package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtScopeGroupConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val values: Set<String>
) : CwtConfig<CwtProperty>