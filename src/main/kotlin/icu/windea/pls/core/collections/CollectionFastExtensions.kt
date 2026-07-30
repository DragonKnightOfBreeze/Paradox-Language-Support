@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.annotations.Fast

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachFast(action: (T) -> Unit) {
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        action(e)
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        action(i, e)
    }
}

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachReversedFast(action: (T) -> Unit) {
    val lastIndex = lastIndex
    for (i in lastIndex downTo 0) {
        val e = this[i]
        action(e)
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachReversedIndexedFast(action: (Int, T) -> Unit) {
    val lastIndex = lastIndex
    for (i in lastIndex downTo 0) {
        val e = this[i]
        action(i, e)
    }
}

/** @see kotlin.collections.map */
@Fast
inline fun <T, R> List<T>.mapFast(transform: (T) -> R): List<R> {
    if (isEmpty()) return emptyList()
    val destination: MutableList<R> = ArrayList(size)
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        val t = transform(e)
        destination.add(t)
    }
    return destination
}

/** @see kotlin.collections.mapNotNull */
@Fast
inline fun <T, R> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    if (isEmpty()) return emptyList()
    var destination: MutableList<R>? = null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        val t = transform(e) ?: continue
        if (destination == null) destination = ArrayList() // delay initialization
        destination.add(t)
    }
    return destination.orEmpty()
}

/** @see kotlin.collections.flatMap */
@Fast
inline fun <T, R> List<T>.flatMapFast(transform: (T) -> Collection<R>): List<R> {
    if (isEmpty()) return emptyList()
    var destination: MutableList<R>? = null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        val t = transform(e)
        if (t.isEmpty()) continue
        if (destination == null) destination = ArrayList() // delay initialization
        destination.addAll(t)
    }
    return destination.orEmpty()
}

/** @see kotlin.collections.filter */
@Fast
inline fun <T> List<T>.filterFast(predicate: (T) -> Boolean): List<T> {
    if (isEmpty()) return emptyList()
    var destination: MutableList<T>? = null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (!predicate(e)) continue
        if (destination == null) destination = ArrayList() // delay initialization
        destination.add(e)
    }
    return destination.orEmpty()
}

/** @see kotlin.collections.filterNotNull */
@Fast
inline fun <T> List<T?>.filterNotNullFast(): List<T> {
    if (isEmpty()) return emptyList()
    var destination: MutableList<T>? = null
    val size = size
    for (i in 0 until size) {
        val e = this[i] ?: continue
        if (destination == null) destination = ArrayList() // delay initialization
        destination.add(e)
    }
    return destination.orEmpty()
}

/** @see filterIsInstance */
@Fast
inline fun <reified R> List<*>.filterIsInstanceFast(predicate: (R) -> Boolean = { true }): List<R> {
    if (isEmpty()) return emptyList()
    var destination: MutableList<R>? = null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (e !is R || !predicate(e)) continue
        if (destination == null) destination = ArrayList() // delay initialization
        destination.add(e)
    }
    return destination.orEmpty()
}

/** @see kotlin.collections.find */
@Fast
inline fun <T> List<T>.findFast(predicate: (T) -> Boolean): T? {
    if (isEmpty()) return null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (predicate(e)) return e
    }
    return null
}

/** @see kotlin.collections.findLast */
@Fast
inline fun <T> List<T>.findLastFast(predicate: (T) -> Boolean): T? {
    if (isEmpty()) return null
    val lastIndex = lastIndex
    for (i in lastIndex downTo 0) {
        val e = this[i]
        if (predicate(e)) return e
    }
    return null
}

/** @see findIsInstance */
inline fun <reified R> List<*>.findIsInstanceFast(predicate: (R) -> Boolean = { true }): R? {
    if (isEmpty()) return null
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (e is R && predicate(e)) return e
    }
    return null
}

/** @see kotlin.collections.all */
@Fast
inline fun <T> List<T>.allFast(predicate: (T) -> Boolean): Boolean {
    if (isEmpty()) return true
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (!predicate(e)) return false
    }
    return true
}

/** @see kotlin.collections.any */
@Fast
inline fun <T> List<T>.anyFast(predicate: (T) -> Boolean): Boolean {
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (predicate(e)) return true
    }
    return false
}

/** @see kotlin.collections.none */
@Fast
inline fun <T> List<T>.noneFast(predicate: (T) -> Boolean): Boolean {
    if (isEmpty()) return true
    val size = size
    for (i in 0 until size) {
        val e = this[i]
        if (predicate(e)) return false
    }
    return true
}
