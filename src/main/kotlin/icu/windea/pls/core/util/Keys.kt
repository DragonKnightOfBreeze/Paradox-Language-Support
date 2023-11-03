@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import java.util.function.*
import kotlin.reflect.*

interface KeyRegistry

inline fun <T> createKey(name: String) = Key.create<T>(name)
inline fun <T> createKey(name: String, factory: Supplier<T>) = KeyWithDefaultValue.create(name, factory)

inline operator fun <T> Key<T>.getValue(thisRef: KeyRegistry, property: KProperty<*>) = this
inline operator fun <T> KeyWithDefaultValue<T>.getValue(thisRef: KeyRegistry, property: KProperty<*>) = this

class KeyDelegates {
    class Normal<T>
    
    class Named<T>(val name: String)
    
    class WithDefaultValue<T>(val factory: Supplier<T>)
    
    class NamedWithDefaultValue<T>(val name: String, val factory: Supplier<T>) 
}

inline fun <T> createKeyDelegate() = KeyDelegates.Normal<T>()
inline fun <T> createKeyDelegate(name: String) = KeyDelegates.Named<T>(name)
inline fun <T> createKeyDelegate(factory: Supplier<T>) = KeyDelegates.WithDefaultValue<T>(factory)
inline fun <T> createKeyDelegate(name: String, factory: Supplier<T>) = KeyDelegates.NamedWithDefaultValue(name, factory)

inline operator fun <T> KeyDelegates.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = createKey<T>(property.name)
inline operator fun <T> KeyDelegates.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = createKey<T>(name)
inline operator fun <T> KeyDelegates.WithDefaultValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = createKey(property.name, factory)
inline operator fun <T> KeyDelegates.NamedWithDefaultValue<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = createKey(name , factory)