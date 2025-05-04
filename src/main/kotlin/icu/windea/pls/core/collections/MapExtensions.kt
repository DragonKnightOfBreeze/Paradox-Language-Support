@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import java.util.*
import kotlin.reflect.*

inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>

inline fun <K, V> Map<K, V>.optimized(): Map<K, V> = if (size <= 1) this.toMap() else this

inline fun <K, V> MutableMap<K, V>.synced(): MutableMap<K, V> = Collections.synchronizedMap(this)

inline fun <K, E, M : MutableMap<K, MutableList<E>>> M.getOrInit(key: K): MutableList<E> = getOrPut(key) { mutableListOf() }

inline fun <K, K1, V1, M : MutableMap<K, MutableMap<K1, V1>>> M.getOrInit(key: K): MutableMap<K1, V1> = getOrPut(key) { mutableMapOf() }

fun <K, V> Map<K, V>.find(predicate: (Map.Entry<K, V>) -> Boolean): V? {
    for (entry in this) {
        if (predicate(entry)) return entry.value
    }
    throw NoSuchElementException()
}

fun <K, V> Map<K, V>.findOrNull(predicate: (Map.Entry<K, V>) -> Boolean): V? {
    for (entry in this) {
        if (predicate(entry)) return entry.value
    }
    return null
}

inline fun <K, V, reified R> Map<K, V>.mapToArray(transform: (Map.Entry<K, V>) -> R): Array<R> {
    //这里不先将Set转为List
    val size = this.size
    val entries = this.entries
    try {
        val iterator = entries.iterator()
        return Array(size) { transform(iterator.next()) }
    } catch (e: Exception) {

        val list = entries.toList()
        return Array(size) { transform(list[it]) }
    }
}

fun <K, V> Map<K, V>.process(processor: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (entry in this) {
        val result = processor(entry)
        if (!result) return false
    }
    return true
}

/**
 * 如果对应键的值不存在，则先将指定的默认值放入映射（当实例化对应的委托属性时即会放入），再提供委托。
 */
class MapWithDefaultValueDelegate<V>(val map: MutableMap<String, V>, val defaultValue: V)

inline operator fun <V> MapWithDefaultValueDelegate<V>.provideDelegate(thisRef: Any?, property: KProperty<*>): MutableMap<String, V> {
    map.putIfAbsent(property.name, defaultValue)
    return map
}

inline infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)
