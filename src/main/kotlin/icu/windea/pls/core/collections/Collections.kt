@file:Suppress("unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.*
import java.util.*

@Suppress("NOTHING_TO_INLINE")
inline fun <T, C: Collection<T>> C?.takeIfNotEmpty() = this?.takeIf { it.isNotEmpty() }

fun <T> Collection<T>.toListOrThis(): List<T> {
	return when(this) {
		is List -> this
		else -> this.toList()
	}
}

fun <T> Collection<T>.toSetOrThis(): Set<T> {
	return when(this) {
		is Set -> this
		else -> this.toSet()
	}
}

fun <T> List<T>.asMutable() = this as MutableList<T>

fun <T> Set<T>.asMutable() = this as MutableSet<T>

fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

fun <K,V> MutableMap<K,V>.synced(): MutableMap<K,V> = Collections.synchronizedMap(this)

inline fun <reified T> Sequence<T>.toArray() = this.toList().toTypedArray()

inline fun <reified R> Iterable<*>.findIsInstance(): R? {
	return findIsInstance(R::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <R> Iterable<*>.findIsInstance(klass: Class<R>): R? {
	for(element in this) if(klass.isInstance(element)) return element as R
	return null
}

inline fun <reified R> Sequence<*>.findIsInstance(): R? {
	return findIsInstance(R::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <R> Sequence<*>.findIsInstance(klass: Class<R>): R? {
	for(element in this) if(klass.isInstance(element)) return element as R
	return null
}


fun <K, V> Map<K, V>.find(predicate: (Map.Entry<K, V>) -> Boolean): V? {
	for(entry in this) {
		if(predicate(entry)) return entry.value
	}
	throw NoSuchElementException()
}

fun <K, V> Map<K, V>.findOrNull(predicate: (Map.Entry<K, V>) -> Boolean): V? {
	for(entry in this) {
		if(predicate(entry)) return entry.value
	}
	return null
}


inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNotNullTo(destination: C, transform: (T) -> R?): C {
	for(item in this) {
		val result = transform(item)
		if(result != null) destination.add(result)
	}
	return destination
}

inline fun <T, R : Any> List<T>.mapAndFirst(predicate: (R?) -> Boolean = { it != null }, transform: (T) -> R?): R? {
	if(this.isEmpty()) return null
	var first: R? = null
	for(element in this) {
		val result = transform(element)
		if(predicate(result)) return result
		first = result
	}
	return first
}

inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
	return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
	return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
	if(this is List) return this.mapToArray(transform)
	val result = arrayOfNulls<R>(this.size)
	for((i, e) in this.withIndex()) {
		result[i] = transform(e)
	}
	return result.cast()
}

inline fun <K, V, reified R> Map<K, V>.mapToArray(transform: (Map.Entry<K, V>) -> R): Array<R> {
	//这里不先将Set转为List
	val size = this.size
	val entries = this.entries
	try {
		val iterator = entries.iterator()
		return Array(size) { transform(iterator.next()) }
	} catch(e: Exception) {
		val list = entries.toList()
		return Array(size) { transform(list[it]) }
	}
}

inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
	return toList().mapToArray(transform)
}


fun <T, E> List<T>.groupAndCountBy(selector: (T) -> E?): Map<E, Int> {
	val result = mutableMapOf<E, Int>()
	for(e in this) {
		val k = selector(e)
		if(k != null) {
			result.compute(k) { _, v -> if(v == null) 1 else v + 1 }
		}
	}
	return result
}


inline fun <T> Iterable<T>.pinned(predicate: (T) -> Boolean): List<T> {
	val result = mutableListOf<T>()
	var elementToPin: T? = null
	for(e in this) {
		if(elementToPin == null && predicate(e)) {
			elementToPin = e
		} else {
			result.add(e)
		}
	}
	if(elementToPin != null) result.add(0, elementToPin)
	return result
}

inline fun <T> Iterable<T>.pinnedLast(predicate: (T) -> Boolean): List<T> {
	val result = mutableListOf<T>()
	var elementToPin: T? = null
	for(e in this) {
		if(elementToPin == null && predicate(e)) {
			elementToPin = e
		} else {
			result.add(e)
		}
	}
	if(elementToPin != null) result.add(elementToPin)
	return result
}