@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core.util

/**
 * 可取反的值。
 *
 * 使用布尔运算符 [operator] 表示正/反语义，常用于条件表达式场景（如带 `!` 前缀）。
 *
 * @property operator 运算符，`true` 表示正向，`false` 表示取反。
 * @property value 原始值。
 */
data class ReversibleValue<T>(
    val operator: Boolean,
    val value: T
) {
    /**
     * 返回取反后的 [ReversibleValue]。
     */
    fun reversed(): ReversibleValue<T> = ReversibleValue(!operator, value)
}

/**
 * 若为正向（[ReversibleValue.operator] 为 `true`）则返回值，否则返回 `null`。
 */
inline fun <T> ReversibleValue<T>.takeWithOperator(): T? = if (operator) value else null

/**
 * 使用谓词 [predicate] 对 [ReversibleValue.value] 进行判断，并依据 [ReversibleValue.operator] 决定是否取反结果。
 *
 * 即：当 [ReversibleValue.operator] 为 `true` 时返回 `predicate(value)`，否则返回其逻辑非。
 */
inline fun <T> ReversibleValue<T>.withOperator(predicate: (T) -> Boolean): Boolean = predicate(value).let { if (operator) it else !it }

/**
 * 从字符串表达式构造 [ReversibleValue]。
 *
 * 以 `!` 前缀表示取反，其余情况为正向。
 */
fun ReversibleValue(expression: String): ReversibleValue<String> {
    return if (expression.startsWith('!')) ReversibleValue(false, expression.drop(1)) else ReversibleValue(true, expression)
}
