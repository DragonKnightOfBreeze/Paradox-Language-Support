@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core.util

data class ReversibleValue<T>(
    override val operator: Boolean,
    val value: T
): Reversible {
    override fun reversed(): ReversibleValue<T> = ReversibleValue(!operator, value)
}

inline fun <T> ReversibleValue<T>.takeWithOperator(): T? = if (operator) value else null

inline fun <T> ReversibleValue<T>.withOperator(predicate: (T) -> Boolean): Boolean = predicate(value).let { if (operator) it else !it }

fun ReversibleValue(expression: String): ReversibleValue<String> {
    return if (expression.startsWith('!')) ReversibleValue(false, expression.drop(1)) else ReversibleValue(true, expression)
}
