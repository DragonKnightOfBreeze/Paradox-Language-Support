package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtSingleAliasConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val name:String,
	override val config: CwtPropertyConfig
): CwtInlineableConfig