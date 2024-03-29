package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

sealed interface CwtInlineableConfig<out T : CwtMemberElement, out C : CwtMemberConfig<T>> : CwtDelegatedConfig<T, C>