@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.cast
import java.util.*

inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>

inline fun <T> Set<T>.asMutable(): MutableSet<T> = this as MutableSet<T>

// only for empty list (since, e.g., string elements may ignore case)
inline fun <T> List<T>.optimized(): List<T> = ifEmpty { emptyList() }

// only for empty list (since, e.g., string elements may ignore case)
inline fun <T : Any> Set<T>.optimized(): Set<T> = ifEmpty { emptySet() }

inline fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

inline fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

inline fun <T> Collection<T>.toListOrThis(): List<T> = if (this is List) this else this.toList()

inline fun <T> Collection<T>.toSetOrThis(): Set<T> = if (this is Set) this else this.toSet()

inline fun <reified R> Iterable<*>.filterIsInstance(predicate: (R) -> Boolean): List<R> {
    return filterIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
inline fun <R> Iterable<*>.filterIsInstance(klass: Class<R>, predicate: (R) -> Boolean): List<R> {
    val result = ArrayList<R>()
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) result.add(element)
    return result
}

inline fun <reified R> Iterable<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
inline fun <R> Iterable<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) return element
    return null
}

inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    if (this is List) return this.mapToArray(transform)
    val result = arrayOfNulls<R>(this.size)
    for ((i, e) in this.withIndex()) {
        result[i] = transform(e)
    }
    return result.cast()
}

inline fun <T> Iterable<T>.pinned(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toListOrThis()
    if (this.none()) return emptyList()
    val result = mutableListOf<T>()
    val elementsToPin = mutableListOf<T>()
    for (e in this) {
        if (predicate(e)) {
            elementsToPin += e
        } else {
            result += e
        }
    }
    return elementsToPin + result
}

inline fun <T> Iterable<T>.pinnedLast(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toListOrThis()
    if (this.none()) return emptyList()
    val result = mutableListOf<T>()
    val elementsToPin = mutableListOf<T>()
    for (e in this) {
        if (predicate(e)) {
            elementsToPin += e
        } else {
            result += e
        }
    }
    return result + elementsToPin
}

fun <T> Iterable<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}

//fun <T> List<T>.toMutableIfNotEmptyInActual(): List<T> {
//    //make List<T> properties mutable in actual if not empty (to hack them if necessary)
//    if (this.isEmpty()) return this
//    if (this is ArrayList || this is LinkedList || this is CopyOnWriteArrayList) return this
//    try {
//        this as MutableList
//        this.removeAt(-1)
//    } catch (e: UnsupportedOperationException) {
//        return this.toMutableList()
//    } catch (e: Exception) {
//        return this
//    }
//    return this
//}
