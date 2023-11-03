@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import java.util.concurrent.*
import java.util.function.*
import kotlin.reflect.*

interface KeyHolder

inline fun <T> createKey(name: String) = Key.create<T>(name)
inline fun <T> createKey(name: String, factory: Supplier<T>) = KeyWithDefaultValue.create(name, factory)

inline operator fun <T> Key<T>.getValue(thisRef: KeyHolder, property: KProperty<*>) = this
inline operator fun <T> KeyWithDefaultValue<T>.getValue(thisRef: KeyHolder, property: KProperty<*>) = this

open class KeyRegistry(val id: String) {
    val keys = ConcurrentHashMap<String, Key<*>>()
}

class KeyDelegates {
    class Normal<T>(val registry: KeyRegistry)
    
    class Named<T>(val registry: KeyRegistry, val name: String)
    
    class WithDefaultValue<T>(val registry: KeyRegistry, val factory: Supplier<T>)
    
    class NamedWithDefaultValue<T>(val registry: KeyRegistry, val name: String, val factory: Supplier<T>)
}

inline fun <T> KeyDelegates.Normal<T>.create(name: String) =
    registry.keys.getOrPut(name) { Key.create<T>("${registry.id}.$name") } as Key<T>

inline fun <T> KeyDelegates.Named<T>.create() =
    registry.keys.getOrPut(name) { Key.create<T>("${registry.id}.$name") } as Key<T>

inline fun <T> KeyDelegates.WithDefaultValue<T>.create(name: String) =
    registry.keys.getOrPut(name) { KeyWithDefaultValue.create("${registry.id}.$name", factory) } as KeyWithDefaultValue<T>

inline fun <T> KeyDelegates.NamedWithDefaultValue<T>.create() =
    registry.keys.getOrPut(name) { KeyWithDefaultValue.create("${registry.id}.$name", factory) } as KeyWithDefaultValue<T>

inline fun <T> createKeyDelegate(registry: KeyRegistry) = KeyDelegates.Normal<T>(registry)

inline fun <T> createKeyDelegate(registry: KeyRegistry, name: String) = KeyDelegates.Named<T>(registry, name)

inline fun <T> createKeyDelegate(registry: KeyRegistry, factory: Supplier<T>) = KeyDelegates.WithDefaultValue(registry, factory)

inline fun <T> createKeyDelegate(registry: KeyRegistry, name: String, factory: Supplier<T>) = KeyDelegates.NamedWithDefaultValue(registry, name, factory)

inline operator fun <T> KeyDelegates.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = create(property.name)

inline operator fun <T> KeyDelegates.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = create()

inline operator fun <T> KeyDelegates.WithDefaultValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = create(property.name)

inline operator fun <T> KeyDelegates.NamedWithDefaultValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = create()
