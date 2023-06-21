package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtSingleAliasConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	override val config: CwtPropertyConfig,
	override val name: String
): CwtInlineableConfig<CwtProperty>