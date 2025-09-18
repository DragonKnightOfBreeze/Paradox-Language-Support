@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

/** 如果当前数组为 `null` 或为空，则返回 `null`。否则返回自身。*/
inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将当前数组映射为新的数组。*/
inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}
