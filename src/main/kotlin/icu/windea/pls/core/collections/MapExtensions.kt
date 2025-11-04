@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

import java.util.*
import kotlin.reflect.KProperty

/** 如果当前映射为 `null` 或为空，则返回 `null`。否则返回自身。*/
inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将当前只读 [Map] 视为 [MutableMap]（要求实际可变，否则抛出异常）。*/
inline fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>

/** 返回加锁后的同步的 [MutableMap]。*/
inline fun <K, V> MutableMap<K, V>.synced(): MutableMap<K, V> = Collections.synchronizedMap(this)

/** 将条目映射为数组；在可迭代器场景下避免中间 [List] 分配。*/
inline fun <K, V, reified R> Map<K, V>.mapToArray(transform: (Map.Entry<K, V>) -> R): Array<R> {
    // 这里不先将Set转为List
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

/** 逐个处理条目，如果处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
inline fun <K, V> Map<K, V>.process(processor: (Map.Entry<K, V>) -> Boolean): Boolean {
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

/** 得到指定键 [key] 对应的类型为 [List] 的值中的最后一个元素，如果不存在则返回 `null`。 */
fun <K, V> Map<K, List<V>>.getOne(key: K): V? = get(key)?.lastOrNull()

/** 得到指定键 [key] 对应的类型为 [List] 的值中的所有元素，如果不存在则返回空列表。 */
fun <K, V> Map<K, List<V>>.getAll(key: K): List<V> = get(key).orEmpty()

/** 得到指定键 [key] 对应的类型为 [MutableList] 的值，如果不存在则先初始化。*/
fun <K, E, M : MutableMap<K, MutableList<E>>> M.getOrInit(key: K): MutableList<E> = getOrPut(key) { mutableListOf() }

/** 得到指定键 [key] 对应的类型为 [MutableMap] 的值，如果不存在则先初始化。*/
fun <K, K1, V1, M : MutableMap<K, MutableMap<K1, V1>>> M.getOrInit(key: K): MutableMap<K1, V1> = getOrPut(key) { mutableMapOf() }

/** 如果当前映射为空或单例，则返回优化后的 [Map]，否则返回自身。用于优化内存。*/
fun <K, V> Map<K, V>.optimized(): Map<K, V> = if (size <= 1) toMap() else this

/** 如果当前映射为空，则返回优化后的 [Map]，否则返回自身。用于优化内存。*/
@Suppress("ReplaceIsEmptyWithIfEmpty")
fun <K, V> Map<K, V>.optimizedIfEmpty(): Map<K, V> = if (isEmpty()) emptyMap() else this
