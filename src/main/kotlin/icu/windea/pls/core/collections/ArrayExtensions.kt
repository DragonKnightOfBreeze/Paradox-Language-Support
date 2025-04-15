@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}
