@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util.values

/**
 * 可以取反的值（包装类）。
 *
 * 说明：
 * - 使用布尔运算符 [operator] 表示正反语义。
 */
data class ReversibleValue<T>(
    val value: T,
    val operator: Boolean
) {
    fun reversed(): ReversibleValue<T> = ReversibleValue(value, !operator)

    operator fun not(): ReversibleValue<T> = reversed()

    fun takeWithOperator(): T? = if (operator) value else null

    inline fun withOperator(predicate: (T) -> Boolean): Boolean = predicate(value).let { if (operator) it else !it }

    companion object {
        /**
         * 从 [expression] 构造 [ReversibleValue]，并在必要时去除首尾空白。如果表达式以 `!` 开始则取反。
         */
        @JvmStatic
        fun from(expression: String): ReversibleValue<String> {
            val s = expression.trim()
            return if (s.startsWith('!')) ReversibleValue(s.drop(1).trimStart(), false) else ReversibleValue(s, true)
        }
    }
}
