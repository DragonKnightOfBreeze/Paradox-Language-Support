@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.annotations.Fast
import it.unimi.dsi.fastutil.objects.ObjectArrayList

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

/** @see kotlin.collections.filterIsInstance */
@Fast
inline fun <reified R> List<*>.filterIsInstanceFast(): List<R> {
    if (isEmpty()) return emptyList()
    val destination = ObjectArrayList<R>()
    forEachFast { e -> if (e is R) destination.add(e) }
    return destination
}
