package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

sealed interface CwtInlineableConfig<out T: CwtMemberElement>: CwtConfig<T> {
	val name: String
	val config: CwtMemberConfig<T>
}