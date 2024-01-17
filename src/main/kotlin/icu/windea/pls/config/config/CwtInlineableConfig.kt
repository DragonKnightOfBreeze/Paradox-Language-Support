package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

interface CwtInlineableConfig<out T: CwtMemberElement>: CwtConfig<T> {
	val name: String
	val config: CwtMemberConfig<T>
}