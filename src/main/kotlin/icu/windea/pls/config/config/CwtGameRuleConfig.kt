package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtGameRuleConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String
): CwtConfig<CwtProperty> 