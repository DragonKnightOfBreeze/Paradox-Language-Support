@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

/** 非空且非空数组时返回自身，否则返回 null。 */
inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将列表按转换函数映射为新数组。 */
inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}
