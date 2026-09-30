@file:Suppress("unused")

package icu.windea.pls.core.ui

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import kotlin.reflect.KMutableProperty0

/**
 * 将当前的 Kotlin 属性转换为原子属性（[AtomicBooleanProperty]）。
 * 绑定原子属性后，修改操作会立即生效。
 */
fun KMutableProperty0<Boolean>.toAtomicProperty(): AtomicBooleanProperty {
    return AtomicBooleanProperty(get()).also { p -> p.afterChange { set(it) } }
}

/**
 * 将当前的 Kotlin 属性转换为原子属性（[AtomicProperty]）。
 * 绑定原子属性后，修改操作会立即生效。
 */
fun <T> KMutableProperty0<T>.toAtomicProperty(): AtomicProperty<T> {
    return AtomicProperty(get()).also { p -> p.afterChange { set(it) } }
}

/**
 * 将当前的 Kotlin 属性转换为原子属性（[AtomicProperty]），并指定默认值（[defaultValue]）。
 * 绑定原子属性后，修改操作会立即生效。
 */
fun <T : Any> KMutableProperty0<T?>.toAtomicProperty(defaultValue: T): AtomicProperty<T> {
    return AtomicProperty(get() ?: defaultValue).also { p -> p.afterChange { set(it) } }
}

/**
 * 基于现有 `KMutableProperty0` 生成 GraphProperty，并在值变化时回写。
 */
fun <V> PropertyGraph.propertyFrom(property: KMutableProperty0<V>): GraphProperty<V> {
    return lazyProperty { property.get() }.apply { afterChange { property.set(it) } }
}

/**
 * 基于 `Map` 的键值生成 GraphProperty，并在值变化时同步到 Map。
 */
fun <K, V> PropertyGraph.propertyFrom(map: MutableMap<K, V>, key: K, defaultValue: V): GraphProperty<V> {
    return lazyProperty { map.getOrPut(key) { defaultValue } }.apply { afterChange { map.put(key, it) } }
}
