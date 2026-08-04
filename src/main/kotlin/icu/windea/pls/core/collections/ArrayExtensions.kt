@file:Suppress("unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.cast

/** 如果当前数组为 `null` 或为空，则返回 `null`。否则返回自身。 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将当前集合转换为新的数组。如果当前数组为空，则直接返回 [empty]。 */
@Suppress("NOTHING_TO_INLINE")
inline fun <E> Collection<E>.toArray(empty: Array<E>): Array<E> {
    val size = size // optimize: cache input size first
    if (size == 0) return empty // optimize: fast return
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
    return (this as java.util.Collection<E>).toArray(empty)
}

/** 将当前数组映射为新的数组。如果当前数组为空，则直接返回 [empty]。 */
inline fun <T, reified R> Array<out T>.mapToArray(empty: Array<R>, transform: (T) -> R): Array<R> {
    val size = size // optimize: cache input size first
    if (size == 0) return empty // optimize: fast return
    return Array(size) { transform(this[it]) }
}

/** 将当前列表映射为数组。如果当前列表为空，则直接返回 [empty]。 */
inline fun <T, reified R> List<T>.mapToArray(empty: Array<R>, transform: (T) -> R): Array<R> {
    val size = size // optimize: cache input size first
    if (size == 0) return empty // optimize: fast return
    return Array(size) { transform(this[it]) }
}

/** 将当前集合映射为数组。如果为列表则按索引遍历，否则顺序遍历。如果当前集合为空，则直接返回 [empty]。 */
inline fun <T, reified R> Collection<T>.mapToArray(empty: Array<R>, transform: (T) -> R): Array<R> {
    val size = size // optimize: cache input size first
    if (size == 0) return empty // optimize: fast return
    if (this is List) return Array(size) { transform(this[it]) }
    val result = arrayOfNulls<R>(size)
    forEachIndexed { i, e -> result[i] = transform(e) }
    return result.cast()
}
