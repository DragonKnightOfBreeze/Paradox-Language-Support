@file:Optimized
@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Key.*
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.asMutable
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import kotlin.reflect.KProperty

// Key Extensions

inline fun <T> createKey(name: String): Key<T> = create<T>(name)

fun Key<*>.clear(target: UserDataHolder) {
    target.putUserData(this, null)
}

fun Key<*>.copy(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
    @Suppress("UNCHECKED_CAST")
    this as Key<Any>
    val v = source.getUserData(this)
    if (ifPresent && v == null) return
    target.putUserData(this, v)
}

// Key Registries

abstract class KeyRegistry {
    val id = javaClass.name.substringAfterLast(".").replace("\$Keys", "")

    // NOTE 3.0.1 optimize: use immutable set as interface, and array backend mutable set as implementation
    val keys: Set<Key<*>> = ObjectArraySet()

    fun <T> find(name: String): Key<T>? {
        return keys.find { it is KeyNamed && it.name == name }.castOrNull()
    }

    fun clear(target: UserDataHolder) {
        keys.forEach { key -> key.clear(target) }
    }

    fun copy(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than `UserDataHolderBase.copyUserDataTo` to reduce memory usage
        keys.forEach { key -> key.copy(source, target, ifPresent) }
    }
}

// Keys

sealed class KeyNamed<T>(val name: String) : Key<T>(name)

class KeyNormal<T>(name: String) : KeyNamed<T>(name)

class KeyWithDefault<T>(name: String, val default: T) : KeyNamed<T>(name)

class KeyWithProducer<T>(name: String, val producer: () -> T) : KeyNamed<T>(name)

class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T) : KeyNamed<T>(name)

// Key Providers

sealed class KeyProvider<T>(val registry: KeyRegistry) {
    protected fun getKeyName(shortName: String): String {
        return "${registry.id}.${shortName}"
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <K : Key<T>> register(name: String, block: () -> K): K {
        val keys = registry.keys
        keys.find { it is KeyNamed }?.let { return it as K }
        // NOTE 3.0.1 optimize: make if mutable with sync only if it's necessary to create and register
        return synchronized(registry) {
            keys.find { it is KeyNamed }?.let { return it as K }
            val key = block()
            keys.asMutable().add(key)
            key
        }
    }
}

fun interface KeyProviderCallback<T> {
    fun call(key: Key<T>, keyName: String)
}

class KeyProviderNormal<T>(registry: KeyRegistry) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyNormal<T> {
        val name = getKeyName(shortName)
        return register(name) { KeyNormal(name) }
    }
}

class KeyProviderWithDefault<T>(registry: KeyRegistry, val default: T) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyWithDefault<T> {
        val name = getKeyName(shortName)
        return register(name) { KeyWithDefault(name, default) }
    }
}

class KeyProviderWithProducer<T>(registry: KeyRegistry, val producer: () -> T) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyWithProducer<T> {
        val name = getKeyName(shortName)
        return register(name) { KeyWithProducer(name, producer) }
    }
}

class KeyProviderWithFactory<T, THIS>(registry: KeyRegistry, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyWithFactory<T, THIS> {
        val name = getKeyName(shortName)
        return register(name) { KeyWithFactory(name, factory) }
    }
}

class KeyProviderNamed<T>(registry: KeyRegistry, val name: String) : KeyProvider<T>(registry) {
    fun getKey(): KeyNormal<T> {
        return register(name) { KeyNormal(name) }
    }
}

class KeyProviderNamedWithDefault<T>(registry: KeyRegistry, val name: String, val default: T) : KeyProvider<T>(registry) {
    fun getKey(): KeyWithDefault<T> {
        return register(name) { KeyWithDefault(name, default) }
    }
}

class KeyProviderNamedWithProducer<T>(registry: KeyRegistry, val name: String, val producer: () -> T) : KeyProvider<T>(registry) {
    fun getKey(): KeyWithProducer<T> {
        return register(name) { KeyWithProducer(name, producer) }
    }
}

class KeyProviderNamedWithFactory<T, THIS>(registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
    fun getKey(): KeyWithFactory<T, THIS> {
        return register(name) { KeyWithFactory(name, factory) }
    }
}

// Register Extensions

inline fun <T> registerKey(registry: KeyRegistry): KeyProviderNormal<T> = KeyProviderNormal(registry)

inline fun <T> registerKey(registry: KeyRegistry, default: T): KeyProviderWithDefault<T> = KeyProviderWithDefault(registry, default)

inline fun <T> registerKey(registry: KeyRegistry, noinline producer: () -> T): KeyProviderWithProducer<T> = KeyProviderWithProducer(registry, producer)

inline fun <T, THIS> registerKeyWithThis(registry: KeyRegistry, noinline factory: THIS.() -> T): KeyProviderWithFactory<T, THIS> = KeyProviderWithFactory(registry, factory)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String): KeyProviderNamed<T> = KeyProviderNamed(registry, name)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String, default: T): KeyProviderNamedWithDefault<T> = KeyProviderNamedWithDefault(registry, name, default)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String, noinline producer: () -> T): KeyProviderNamedWithProducer<T> = KeyProviderNamedWithProducer(registry, name, producer)

inline fun <T, THIS> registerNamedKeyWithThis(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T): KeyProviderNamedWithFactory<T, THIS> = KeyProviderNamedWithFactory(registry, name, factory)

// Delegate Extensions

inline operator fun <T, K : Key<T>> K.getValue(thisRef: KeyRegistry, property: KProperty<*>): K = this

inline operator fun <T> KeyProviderNormal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyNormal<T> = getKey(property.name)

inline operator fun <T> KeyProviderWithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithDefault<T> = getKey(property.name)

inline operator fun <T> KeyProviderWithProducer<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithProducer<T> = getKey(property.name)

inline operator fun <T, THIS> KeyProviderWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey(property.name)

inline operator fun <T> KeyProviderNamed<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyNormal<T> = getKey()

inline operator fun <T> KeyProviderNamedWithProducer<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithProducer<T> = getKey()

inline operator fun <T> KeyProviderNamedWithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithDefault<T> = getKey()

inline operator fun <T, THIS> KeyProviderNamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey()
