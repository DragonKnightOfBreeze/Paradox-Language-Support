package icu.windea.pls.lang.match

data class ParadoxMatchResultValue<out T>(
    val value: T,
    val result: ParadoxMatchResult
)
