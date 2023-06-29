@file:Suppress("unused")

package icu.windea.pls.core.collections

import kotlin.reflect.*

/**
 * 如果对应键的值不存在，则先将指定的默认值放入映射（当实例化对应的委托属性时即会放入），再提供委托。
 */
class MapWithDefaultValueDelegate<V>(private val map: MutableMap<String, V>, private val defaultValue: V) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): MutableMap<String, V> {
        map.putIfAbsent(property.name, defaultValue)
        return map
    }
}

infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)
