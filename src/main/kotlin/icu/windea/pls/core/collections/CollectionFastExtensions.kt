@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.annotations.Fast

// @Fast
// inline fun <T, R> List<T>.ifNotEmpty(transform: List<T>.() -> List<R>): List<R> {
//     return if (isEmpty()) emptyList() else transform(this)
// }

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachFast(action: (T) -> Unit) {
    val size = size
    for (i in 0 until size) {
        action(this[i])
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = size
    for (i in 0 until size) {
        action(i, this[i])
    }
}

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachReversedFast(action: (T) -> Unit) {
    val lastIndex = lastIndex
    for (i in lastIndex downTo 0) {
        action(this[i])
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachReversedIndexedFast(action: (Int, T) -> Unit) {
    val lastIndex = lastIndex
    for (i in lastIndex downTo 0) {
        action(i, this[i])
    }
}

/** @see kotlin.collections.filterIsInstance */
@Fast
inline fun <reified R> List<*>.filterIsInstanceFast(): List<R> {
    if (isEmpty()) return emptyList()
    val destination = FastList<R>()
    forEachFast { e -> if (e is R) destination.add(e) }
    return destination
}
