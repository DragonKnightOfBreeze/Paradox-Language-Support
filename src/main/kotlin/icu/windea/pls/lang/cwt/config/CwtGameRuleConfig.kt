package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtGameRuleConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String
): CwtConfig<CwtProperty> 