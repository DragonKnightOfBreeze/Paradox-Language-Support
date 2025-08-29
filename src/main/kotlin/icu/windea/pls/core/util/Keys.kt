@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import icu.windea.pls.core.cast
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T) : Key<T>(name)

inline fun <T> createKey(name: String): Key<T> = Key.create<T>(name)

inline fun <T, THIS> createKey(name: String, noinline factory: THIS.() -> T): KeyWithFactory<T, THIS> = KeyWithFactory(name, factory)

abstract class KeyRegistry {
    val id = javaClass.name.substringAfterLast(".").replace("\$Keys", "")
    val keys: MutableMap<String, Key<*>> = ConcurrentHashMap()

    fun getKeyName(propName: String): String {
        return "${id}.${propName}"
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getKey(name: String): Key<T>? {
        return keys.get(name) as? Key<T>
    }
}

abstract class KeyProvider<T> {
    protected var callback: ((Key<T>) -> Unit)? = null

    protected fun Key<T>.runCallback(): Key<T> {
        callback?.invoke(this)
        return this
    }

    fun callback(action: (Key<T>) -> Unit) {
        this.callback = action
    }
}

interface KeyProviders {
    class Normal<T>(val registry: KeyRegistry): KeyProvider<T>() {
        fun getKey(propName: String): Key<T> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    class Named<T>(val registry: KeyRegistry, val name: String): KeyProvider<T>() {
        fun getKey(): Key<T> {
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T): KeyProvider<T>() {
        fun getKey(propName: String): KeyWithFactory<T, THIS> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }

    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(): KeyWithFactory<T, THIS> {
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }
}

inline fun <T> createKey(registry: KeyRegistry) = KeyProviders.Normal<T>(registry)

inline fun <T> createKey(registry: KeyRegistry, name: String) = KeyProviders.Named<T>(registry, name)

inline fun <T, THIS> createKey(registry: KeyRegistry, noinline factory: THIS.() -> T) = KeyProviders.WithFactory(registry, factory)

inline fun <T, THIS> createKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T) = KeyProviders.NamedWithFactory(registry, name, factory)

inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

inline operator fun <T, THIS : KeyRegistry> Key<T>.getValue(thisRef: THIS, property: KProperty<*>) = this
