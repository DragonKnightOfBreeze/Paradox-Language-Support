package icu.windea.pls.config.config

sealed interface CwtInlineableConfig<out C: CwtConfig<*>> {
    val config: C
}
