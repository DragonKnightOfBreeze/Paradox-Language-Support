@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Key.*
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

// Key Extensions

inline fun <T> createKey(name: String): Key<T> = create<T>(name)

fun Key<*>.clear(target: UserDataHolder) {
    target.putUserData(this, null)
}

@Suppress("UNCHECKED_CAST")
fun Key<*>.copy(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
    this as Key<Any>
    val v = source.getUserData(this)
    if (ifPresent && v == null) return
    target.putUserData(this, v)
}

// Key Registries

abstract class KeyRegistry {
    val id = javaClass.name.replace("\$Keys", "")
    val keys: MutableMap<String, Key<*>> = ConcurrentHashMap()

    fun getKeyName(shortName: String): String {
        return "${id}.${shortName}"
    }

    fun <T> getKey(name: String): Key<T> {
        return keys.get(name).cast()
    }

    fun <T> getKeyOrNull(name: String): Key<T>? {
        return keys.get(name).castOrNull()
    }

    fun clear(target: UserDataHolder) {
        keys.values.forEach { key -> key.clear(target) }
    }

    fun copy(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than `UserDataHolderBase.copyUserDataTo` to reduce memory usage
        keys.values.forEach { key -> key.copy(source, target, ifPresent) }
    }
}

abstract class KeyRegistrySynced : KeyRegistry() {
    val syncedKeys: MutableMap<String, Key<*>> = ConcurrentHashMap()

    fun sync(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than `UserDataHolderBase.copyUserDataTo` to reduce memory usage
        syncedKeys.values.forEach { key -> key.copy(source, target, ifPresent) }
    }
}

// Keys

class KeyNormal<T>(val name: String) : Key<T>(name)

class KeyWithDefault<T>(val name: String, val default: T) : Key<T>(name)

class KeyWithFactory<T, in THIS>(val name: String, val factory: THIS.() -> T) : Key<T>(name)

// Key Providers

sealed class KeyProvider<T>(val registry: KeyRegistry) {
    private val callbacks: MutableSet<KeyProviderCallback<T>> = ObjectArraySet()

    fun addCallback(callback: KeyProviderCallback<T>) {
        callbacks += callback
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <K : Key<T>> register(name: String, block: () -> K): K {
        return registry.keys.computeIfAbsent(name) {
            val key = block()
            callbacks.forEach { it.call(key, name) }
            callbacks.clear()
            key
        } as K
    }
}

fun interface KeyProviderCallback<T> {
    fun call(key: Key<T>, keyName: String)
}

class KeyProviderNormal<T>(registry: KeyRegistry) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyNormal<T> {
        val name = registry.getKeyName(shortName)
        return register(name) { KeyNormal(name) }
    }
}

class KeyProviderWithDefault<T>(registry: KeyRegistry, val default: T) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyWithDefault<T> {
        val name = registry.getKeyName(shortName)
        return register(name) { KeyWithDefault(name, default) }
    }
}

class KeyProviderWithFactory<T, THIS>(registry: KeyRegistry, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
    fun getKey(shortName: String): KeyWithFactory<T, THIS> {
        val name = registry.getKeyName(shortName)
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

class KeyProviderNamedWithFactory<T, THIS>(registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
    fun getKey(): KeyWithFactory<T, THIS> {
        return register(name) { KeyWithFactory(name, factory) }
    }
}

// KeyProvider Extensions

fun <P : KeyProvider<T>, T> P.withCallback(callback: KeyProviderCallback<T>) = apply { addCallback(callback) }

fun <P : KeyProvider<T>, T> P.withSync() = withCallback { key, name -> if (registry is KeyRegistrySynced) registry.syncedKeys[name] = key }

// Register Extensions

inline fun <T> registerKey(registry: KeyRegistry): KeyProviderNormal<T> = KeyProviderNormal(registry)

inline fun <T> registerKey(registry: KeyRegistry, default: T): KeyProviderWithDefault<T> = KeyProviderWithDefault(registry, default)

inline fun <T, THIS> registerKey(registry: KeyRegistry, noinline factory: THIS.() -> T): KeyProviderWithFactory<T, THIS> = KeyProviderWithFactory(registry, factory)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String): KeyProviderNamed<T> = KeyProviderNamed(registry, name)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String, default: T): KeyProviderNamedWithDefault<T> = KeyProviderNamedWithDefault(registry, name, default)

inline fun <T, THIS> registerNamedKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T): KeyProviderNamedWithFactory<T, THIS> = KeyProviderNamedWithFactory(registry, name, factory)

// Delegate Extensions

inline operator fun <T, K : Key<T>> K.getValue(thisRef: KeyRegistry, property: KProperty<*>): K = this

inline operator fun <T> KeyProviderNormal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyNormal<T> = getKey(property.name)

inline operator fun <T> KeyProviderWithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithDefault<T> = getKey(property.name)

inline operator fun <T, THIS> KeyProviderWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey(property.name)

inline operator fun <T> KeyProviderNamed<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyNormal<T> = getKey()

inline operator fun <T> KeyProviderNamedWithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithDefault<T> = getKey()

inline operator fun <T, THIS> KeyProviderNamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey()
