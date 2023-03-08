package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

interface CwtInlineableConfig: CwtConfig<CwtProperty> {
	val name: String
	val config: CwtPropertyConfig
}