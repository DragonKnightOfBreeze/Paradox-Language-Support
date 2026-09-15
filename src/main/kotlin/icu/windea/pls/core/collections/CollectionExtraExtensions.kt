package icu.windea.pls.core.collections

import kotlin.reflect.KProperty

/** 得到指定键 [key] 对应的类型为 [List] 的值中的最后一个元素，如果不存在则返回 `null`。 */
fun <K, V> Map<K, List<V>>.getOne(key: K): V? = get(key)?.lastOrNull()

/** 得到指定键 [key] 对应的类型为 [List] 的值中的所有元素，如果不存在则返回空列表。 */
fun <K, V> Map<K, List<V>>.getAll(key: K): List<V> = get(key).orEmpty()

/**
 * 如果对应键的值不存在，则先将指定的默认值放入映射（当实例化对应的委托属性时即会放入），再提供委托。
 */
class MapWithDefaultValueDelegate<V>(val map: MutableMap<String, V>, val defaultValue: V)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <V> MapWithDefaultValueDelegate<V>.provideDelegate(thisRef: Any?, property: KProperty<*>): MutableMap<String, V> {
    map.putIfAbsent(property.name, defaultValue)
    return map
}

/** 为 [MutableMap] 提供默认值委托构建器。 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)
