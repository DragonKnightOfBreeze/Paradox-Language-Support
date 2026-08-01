@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core.collections

import com.google.common.collect.ImmutableList
import icu.windea.pls.core.annotations.Fast

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachFast(action: (T) -> Unit) {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        action(e)
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        action(i, e)
    }
}

/** @see kotlin.collections.forEach */
@Fast
inline fun <T> List<T>.forEachReversedFast(action: (T) -> Unit) {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    for (i in lastIndex downTo 0) { // optimize: use index-based iteration
        val e = this[i]
        action(e)
    }
}

/** @see kotlin.collections.forEachIndexed */
@Fast
inline fun <T> List<T>.forEachReversedIndexedFast(action: (Int, T) -> Unit) {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    for (i in lastIndex downTo 0) { // optimize: use index-based iteration
        val e = this[i]
        action(i, e)
    }
}

/** @see kotlin.collections.map */
@Fast
inline fun <T, R : Any> List<T>.mapFast(transform: (T) -> R): List<R> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    if (size == 1) return ImmutableList.of(transform(this[0])) // optimize: fast return
    val result = arrayOfNulls<Any?>(size) // optimize: construct size array directly for better performance and memory
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        val t = transform(e)
        result[i] = t
    }
    @Suppress("UNCHECKED_CAST")
    return ImmutableList.copyOf(result as Array<out R>)
}

/** @see kotlin.collections.mapNotNull */
@Fast
inline fun <T, R : Any> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    var first: R? = null // optimize: delay list initialization
    var result: MutableList<R>? = null // optimize: delay list initialization
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        val r = transform(e) ?: continue
        when {
            first == null -> first = r
            result == null -> {
                result = ArrayList(size.coerceAtMost(10))
                result.add(first)
                result.add(r)
            }
            else -> result.add(r)
        }
    }
    return result ?: first?.let { ImmutableList.of(it) } ?: ImmutableList.of()
}

/** @see kotlin.collections.flatMap */
@Fast
inline fun <T, R : Any> List<T>.flatMapFast(transform: (T) -> Collection<R>): List<R> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    var result: MutableList<R>? = null
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        val t = transform(e)
        if (t.isEmpty()) continue
        if (result == null) result = ArrayList()
        result.addAll(t)
    }
    return result ?: ImmutableList.of()
}

/** @see kotlin.collections.filter */
@Fast
inline fun <T : Any> List<T>.filterFast(predicate: (T) -> Boolean): List<T> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    var first: T? = null // optimize: delay list initialization
    var result: MutableList<T>? = null // optimize: delay list initialization
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (!predicate(e)) continue
        when {
            first == null -> first = e
            result == null -> {
                result = ArrayList(size.coerceAtMost(10))
                result.add(first)
                result.add(e)
            }
            else -> result.add(e)
        }
    }
    return result ?: first?.let { ImmutableList.of(it) } ?: ImmutableList.of()
}

/** @see kotlin.collections.filterNotNull */
@Fast
fun <T : Any> List<T?>.filterNotNullFast(): List<T> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    var first: T? = null // optimize: delay list initialization
    var result: MutableList<T>? = null // optimize: delay list initialization
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i] ?: continue
        when {
            first == null -> first = e
            result == null -> {
                result = ArrayList(size.coerceAtMost(10))
                result.add(first)
                result.add(e)
            }
            else -> result.add(e)
        }
    }
    return result ?: first?.let { ImmutableList.of(it) } ?: ImmutableList.of()
}

/** @see filterIsInstance */
@Fast
inline fun <reified R : Any> List<*>.filterIsInstanceFast(predicate: (R) -> Boolean = { true }): List<R> {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    if (size == 0) return ImmutableList.of() // optimize: fast return
    var first: R? = null // optimize: delay list initialization
    var result: MutableList<R>? = null // optimize: delay list initialization
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (e !is R || !predicate(e)) continue
        when {
            first == null -> first = e
            result == null -> {
                result = ArrayList(size.coerceAtMost(10))
                result.add(first)
                result.add(e)
            }
            else -> result.add(e)
        }
    }
    return result ?: first?.let { ImmutableList.of(it) } ?: ImmutableList.of()
}

/** @see kotlin.collections.find */
@Fast
inline fun <T> List<T>.findFast(predicate: (T) -> Boolean): T? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (predicate(e)) return e
    }
    return null
}

/** @see kotlin.collections.findLast */
@Fast
inline fun <T> List<T>.findLastFast(predicate: (T) -> Boolean): T? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    for (i in lastIndex downTo 0) { // optimize: use index-based iteration
        val e = this[i]
        if (predicate(e)) return e
    }
    return null
}

/** @see findIsInstance */
inline fun <reified R> List<*>.findIsInstanceFast(predicate: (R) -> Boolean = { true }): R? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (e is R && predicate(e)) return e
    }
    return null
}

/** @see findLastIsInstance */
inline fun <reified R> List<*>.findLastIsInstanceFast(predicate: (R) -> Boolean = { true }): R? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    for (i in lastIndex downTo 0) { // optimize: use index-based iteration
        val e = this[i]
        if (e is R && predicate(e)) return e
    }
    return null
}

/** @see kotlin.collections.all */
@Fast
inline fun <T> List<T>.allFast(predicate: (T) -> Boolean): Boolean {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (!predicate(e)) return false
    }
    return true
}

/** @see kotlin.collections.any */
@Fast
inline fun <T> List<T>.anyFast(predicate: (T) -> Boolean): Boolean {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (predicate(e)) return true
    }
    return false
}

/** @see kotlin.collections.none */
@Fast
inline fun <T> List<T>.noneFast(predicate: (T) -> Boolean): Boolean {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (predicate(e)) return false
    }
    return true
}

/** @see process */
@Fast
inline fun <T> List<T>.processFast(processor: (T) -> Boolean): Boolean {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    for (i in 0 until size) { // optimize: use index-based iteration
        val e = this[i]
        if (!processor(e)) return false
    }
    return true
}
