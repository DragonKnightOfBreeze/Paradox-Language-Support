@file:Suppress("unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.*
import java.util.*

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Array<T>?.takeIfNotEmpty() = this?.takeIf { it.isNotEmpty() }

@Suppress("NOTHING_TO_INLINE")
inline fun <T: Collection<*>> T?.takeIfNotEmpty() = this?.takeIf { it.isNotEmpty() }

@Suppress("NOTHING_TO_INLINE")
inline fun <T: Map<*, *>> T?.takeIfNotEmpty() = this?.takeIf { it.isNotEmpty() }

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Collection<T>.toListOrThis(): List<T> = if(this is List) this else this.toList()

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Collection<T>.toSetOrThis(): Set<T> = if(this is Set) this else this.toSet()

@Suppress("NOTHING_TO_INLINE")
inline fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Set<T>.asMutable(): MutableSet<T> = this as MutableSet<T>

@Suppress("NOTHING_TO_INLINE")
inline fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <K,V> MutableMap<K,V>.synced(): MutableMap<K,V> = Collections.synchronizedMap(this)

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

fun <T> Iterable<T>.process(processor: (T) -> Boolean): Boolean {
	for(e in this) {
		val result = processor(e)
		if(!result) return false
	}
	return true
}

fun <K, V> Map<K, V>.process(processor: (Map.Entry<K, V>) -> Boolean): Boolean {
	for(entry in this) {
		val result = processor(entry)
		if(!result) return false
	}
	return true
}

