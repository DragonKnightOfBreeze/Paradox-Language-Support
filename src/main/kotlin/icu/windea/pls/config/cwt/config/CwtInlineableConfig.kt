package icu.windea.pls.config.cwt.config

import icu.windea.pls.cwt.psi.*

interface CwtInlineableConfig: CwtConfig<CwtProperty> {
	val name: String
	val config: CwtPropertyConfig
}