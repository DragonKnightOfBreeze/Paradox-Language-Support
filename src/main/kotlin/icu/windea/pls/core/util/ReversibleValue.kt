@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

data class ReversibleValue<T>(
    val operator: Boolean,
    val value: T
)

infix fun <T> T.reverseIf(operator: Boolean) = ReversibleValue(operator, this)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> ReversibleValue<T>.takeIfTrue() = if(operator) value else null

@Suppress("NOTHING_TO_INLINE")
inline fun <T> ReversibleValue<T>.takeIfFalse() = if(operator) null else value

inline fun <T> ReversibleValue<T>.where(predicate: (T) -> Boolean) = if(operator) predicate(value) else !predicate(value)

fun ReversibleValue(value: String): ReversibleValue<String> {
    return if(value.startsWith('!')) ReversibleValue(false, value.drop(1)) else ReversibleValue(true, value)
}