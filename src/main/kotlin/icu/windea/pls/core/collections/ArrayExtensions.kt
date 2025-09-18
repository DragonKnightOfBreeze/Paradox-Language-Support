@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

/**
 * 当数组为 `null` 或为空时返回 `null`，否则返回原数组本身。
 *
 * 便于与可空链式调用配合，减少对空数组的分支判断。
 */
inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

/**
 * 将当前数组按顺序映射为新的 [Array]。
 *
 * 预先按 [size] 分配目标数组，并对每个下标应用 [transform]；
 * 相比先转 [List] 再 `map` 的方式更轻量。
 */
inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}
