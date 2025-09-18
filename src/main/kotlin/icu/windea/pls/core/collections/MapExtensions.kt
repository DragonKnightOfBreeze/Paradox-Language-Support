@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.collections

import java.util.*
import kotlin.reflect.KProperty

/** 当映射为 `null` 或为空时返回 `null`，否则返回自身。*/
inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将只读 [Map] 视为 [MutableMap]（要求实际类型可变，否则抛出异常）。*/
inline fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>

// only for empty list (since, e.g., string elements may ignore case)
/** 若映射为空则返回标准库空映射，否则返回自身（减少空集合分配）。*/
inline fun <K, V> Map<K, V>.optimized(): Map<K, V> = ifEmpty { emptyMap() }

/** 返回加锁包装的 [MutableMap]，基于 [Collections.synchronizedMap]。*/
inline fun <K, V> MutableMap<K, V>.synced(): MutableMap<K, V> = Collections.synchronizedMap(this)

/** 获取键 [key] 对应的 [MutableList]，若不存在则初始化后返回。*/
inline fun <K, E, M : MutableMap<K, MutableList<E>>> M.getOrInit(key: K): MutableList<E> = getOrPut(key) { mutableListOf() }

/** 获取键 [key] 对应的嵌套 [MutableMap]，若不存在则初始化后返回。*/
inline fun <K, K1, V1, M : MutableMap<K, MutableMap<K1, V1>>> M.getOrInit(key: K): MutableMap<K1, V1> = getOrPut(key) { mutableMapOf() }

/** 将条目映射为数组；在可迭代器场景下避免中间 [List] 分配。*/
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

/** 逐个处理条目，若处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
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

/** 为 [MutableMap] 提供默认值委托构建器，语法：`myMap withDefault defaultValue`。*/
inline infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)
