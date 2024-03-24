@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import java.util.concurrent.*
import kotlin.reflect.*

class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T): Key<T>(name)

open class KeyRegistry(val id: String = "") {
    val keys = ConcurrentHashMap<String, Key<*>>()
    
    fun getKeyName(name: String) : String {
        if(id.isEmpty()) return name
        return "${id}.${name}"
    }
}

inline fun <T> createKey(name: String) = Key<T>(name)

inline fun <T, THIS> createKey(name: String, noinline factory: THIS.() -> T) = KeyWithFactory(name, factory)

inline operator fun <T> Key<T>.getValue(thisRef: KeyRegistry, property: KProperty<*>) = this

class KeyDelegates {
    class Normal<T>(val registry: KeyRegistry)
    
    class Named<T>(val registry: KeyRegistry, val name: String)
    
    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T)
    
    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T)
}

inline fun <T> KeyDelegates.Normal<T>.getKey(name: String): Key<T> {
    val keyName = registry.getKeyName(name)
    return registry.keys.getOrPut(name) { createKey<T>(keyName) }.cast()
}

inline fun <T> KeyDelegates.Named<T>.getKey(): Key<T> {
    val keyName = registry.getKeyName(name)
    return registry.keys.getOrPut(name) { createKey<T>(keyName) }.cast()
}

inline fun <T, THIS> KeyDelegates.WithFactory<T, THIS>.getKey(name: String): KeyWithFactory<T, THIS> {
    val keyName = registry.getKeyName(name)
    return registry.keys.getOrPut(name) { createKey(keyName, factory) }.cast()
}

inline fun <T, THIS> KeyDelegates.NamedWithFactory<T, THIS>.getKey(): KeyWithFactory<T, THIS> {
    val keyName = registry.getKeyName(name)
    return registry.keys.getOrPut(name) { createKey(keyName, factory) }.cast()
}

inline fun <T> createKeyDelegate(registry: KeyRegistry) = KeyDelegates.Normal<T>(registry)

inline fun <T> createKeyDelegate(registry: KeyRegistry, name: String) = KeyDelegates.Named<T>(registry, name)

inline fun <T, THIS> createKeyDelegate(registry: KeyRegistry, noinline factory: THIS.() -> T) = KeyDelegates.WithFactory(registry, factory)

inline fun <T, THIS> createKeyDelegate(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T) = KeyDelegates.NamedWithFactory(registry, name, factory)

inline operator fun <T> KeyDelegates.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T> KeyDelegates.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS> KeyDelegates.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T, THIS> KeyDelegates.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS: UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserData(this) ?: factory(thisRef).also { thisRef.putUserData(this, it) }
}

inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.get(this) ?: factory(thisRef).also { thisRef.put(this, it) }
}