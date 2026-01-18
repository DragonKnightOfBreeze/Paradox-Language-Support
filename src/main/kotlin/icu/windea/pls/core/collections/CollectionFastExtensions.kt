@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.annotations.Fast

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

/** @see kotlin.collections.map */
@Fast
inline fun <T, R> List<T>.mapFast(transform: (T) -> R): List<R> {
    if (isEmpty()) return emptyList()
    val destination = FastList<R>(size)
    forEachFast { e -> destination.add(transform(e)) }
    return destination
}

/** @see kotlin.collections.mapNotNull */
@Fast
inline fun <T, R> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    if (isEmpty()) return emptyList()
    val destination = FastList<R>(size)
    forEachFast { e -> transform(e)?.let { destination.add(it) } }
    return destination
}

/** @see kotlin.collections.filter */
@Fast
inline fun <T> List<T>.filterFast(predicate: (T) -> Boolean): List<T> {
    if (isEmpty()) return emptyList()
    val destination = FastList<T>()
    forEachFast { e -> if (predicate(e)) destination.add(e) }
    return destination
}

/** @see kotlin.collections.filterNotNull */
@Fast
inline fun <T> List<T?>.filterNotNullFast(): List<T> {
    if (isEmpty()) return emptyList()
    val destination = FastList<T>()
    forEachFast { e -> if (e != null) destination.add(e) }
    return destination
}

// /** @see kotlin.collections.filterIsInstance */
// @Fast
// inline fun <reified R> List<*>.filterIsInstanceFast(): List<R> {
//     if (isEmpty()) return emptyList()
//     val destination = FastList<R>()
//     forEachFast { e -> if (e is R) destination.add(e) }
//     return destination
// }
