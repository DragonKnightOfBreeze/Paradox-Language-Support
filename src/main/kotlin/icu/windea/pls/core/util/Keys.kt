@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import java.util.concurrent.*
import kotlin.reflect.*

class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T) : Key<T>(name)

inline fun <T> createKey(name: String) = Key.create<T>(name)

inline fun <T, THIS> createKey(name: String, noinline factory: THIS.() -> T) = KeyWithFactory(name, factory)

abstract class KeyRegistry {
    val id = javaClass.name.substringAfterLast(".").replace("\$Keys", "")
    val keys = ConcurrentHashMap<String, Key<*>>()
    
    fun getKeyName(propName: String): String {
        return "${id}.${propName}"
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T> getKey(name: String): Key<T>? {
        return keys.get(name) as? Key<T>
    }
}

interface KeyProviders {
    class Normal<T> (val registry: KeyRegistry){
        fun getKey(propName: String): Key<T> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey<T>(name) }.cast()
        }
    }
    
    class Named<T>(val registry: KeyRegistry, val name: String) {
        fun getKey(): Key<T>  {
            return registry.keys.getOrPut(name) { createKey<T>(name) }.cast()
        }
    }
    
    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T){
        fun getKey(propName: String): Key<T>  {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey(name, factory) }.cast()
        }
    }
    
    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T)  {
        fun getKey(): Key<T>  {
            return registry.keys.getOrPut(name) { createKey(name, factory) }.cast()
        }
    }
}

inline fun <T> createKey(registry: KeyRegistry) = KeyProviders.Normal<T>(registry)

inline fun <T> createKey(registry: KeyRegistry, name: String) = KeyProviders.Named<T>(registry,name)

inline fun <T, THIS> createKey(registry: KeyRegistry, noinline factory: THIS.() -> T) = KeyProviders.WithFactory(registry,factory)

inline fun <T, THIS> createKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T) = KeyProviders.NamedWithFactory(registry,name, factory)

inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS : KeyRegistry> Key<T>.getValue(thisRef: THIS, property: KProperty<*>) = this

interface KeyDelegates {
    class Normal<T>(val registry: KeyRegistry) {
        fun getKey(propName: String): Key<T> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey<T>(name) }.cast()
        }
    }
    
    class Named<T>(val registry: KeyRegistry, val name: String) {
        fun getKey(): Key<T> {
            return registry.keys.getOrPut(name) { createKey<T>(name) }.cast()
        }
    }
    
    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T) {
        fun getKey(propName: String): KeyWithFactory<T, THIS> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey(name, factory) }.cast()
        }
    }
    
    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T) {
        fun getKey(): KeyWithFactory<T, THIS> {
            return registry.keys.getOrPut(name) { createKey(name, factory) }.cast()
        }
    }
}

inline fun <T> createKeyDelegate(registry: KeyRegistry) = KeyDelegates.Normal<T>(registry)

inline fun <T> createKeyDelegate(registry: KeyRegistry, name: String) = KeyDelegates.Named<T>(registry, name)

inline fun <T, THIS> createKeyDelegate(registry: KeyRegistry, noinline factory: THIS.() -> T) = KeyDelegates.WithFactory(registry, factory)

inline fun <T, THIS> createKeyDelegate(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T) = KeyDelegates.NamedWithFactory(registry, name, factory)

inline operator fun <T> KeyDelegates.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T> KeyDelegates.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS> KeyDelegates.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T, THIS> KeyDelegates.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()
