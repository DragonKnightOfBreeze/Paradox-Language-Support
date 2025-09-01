@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.collections

import java.util.*
import kotlin.reflect.KProperty

/** 非空且非空映射时返回自身，否则返回 null。 */
inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将不可变 Map 视为可变 Map（未做拷贝，需确保实际类型可变）。 */
inline fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>

// only for empty list (since, e.g., string elements may ignore case)
/** 若为空则返回共享空映射以优化内存。仅适用于空映射场景。 */
inline fun <K, V> Map<K, V>.optimized(): Map<K, V> = ifEmpty { emptyMap() }

/** 返回同步包装的可变 Map。 */
inline fun <K, V> MutableMap<K, V>.synced(): MutableMap<K, V> = Collections.synchronizedMap(this)

/** 取得或初始化列表值的 Map 条目。 */
inline fun <K, E, M : MutableMap<K, MutableList<E>>> M.getOrInit(key: K): MutableList<E> = getOrPut(key) { mutableListOf() }

/** 取得或初始化嵌套 Map 条目。 */
inline fun <K, K1, V1, M : MutableMap<K, MutableMap<K1, V1>>> M.getOrInit(key: K): MutableMap<K1, V1> = getOrPut(key) { mutableMapOf() }

/** 将 Map 条目映射为新数组（尝试使用迭代器避免中间集合）。 */
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

/** 按处理器遍历 Map 条目；当处理器返回 false 时提前终止。 */
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

/** 在属性委托阶段确保为属性名对应的键设置默认值。 */
inline operator fun <V> MapWithDefaultValueDelegate<V>.provideDelegate(thisRef: Any?, property: KProperty<*>): MutableMap<String, V> {
    map.putIfAbsent(property.name, defaultValue)
    return map
}

/** 为 `MutableMap<String, V>` 提供默认值的属性委托。 */
inline infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)
