@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.annotations.Fast

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachFast(action: (T) -> Unit) {
    val size = this.size
    for (i in 0 until size) {
        action(this[i])
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = this.size
    for (i in 0 until size) {
        action(i, this[i])
    }
}

@Fast
inline fun <T, R> List<T>.ifNotEmpty(transform: List<T>.() -> List<R>): List<R> {
    return if (this.isEmpty()) emptyList() else transform(this)
}
