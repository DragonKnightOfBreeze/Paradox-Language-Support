@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

inline fun <T> createKey(name: String): Key<T> = Key.create<T>(name)

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

abstract class KeyRegistry {
    val id = javaClass.name.substringAfterLast(".").replace("\$Keys", "")
    val keys: MutableMap<String, RegistedKey<*>> = ConcurrentHashMap()

    fun getKeyName(shortName: String): String {
        return "${id}.${shortName}"
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getKey(name: String): RegistedKey<T> {
        return keys.get(name) as RegistedKey<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getKeyOrNull(name: String): RegistedKey<T>? {
        return keys.get(name) as? RegistedKey<T>
    }

    fun clear(target: UserDataHolder) {
        keys.values.forEach { key -> key.clear(target) }
    }

    fun copy(source: UserDataHolder, to: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than `UserDataHolderBase.copyUserDataTo` to reduce memory usage
        keys.values.forEach { key -> key.copy(source, to, ifPresent) }
    }
}

abstract class KeyRegistryWithSync : KeyRegistry() {
    val keysToSync: MutableMap<String, RegistedKey<*>> = ConcurrentHashMap()

    fun sync(source: UserDataHolder, target: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than `UserDataHolderBase.copyUserDataTo` to reduce memory usage
        keysToSync.values.forEach { key -> key.copy(source, target, ifPresent) }
    }
}

open class RegistedKey<T>(val registry: KeyRegistry, val name: String) : Key<T>(name)

class RegistedKeyWithDefault<T>(registry: KeyRegistry, name: String, val defaultValue: T) : RegistedKey<T>(registry, name)

class RegistedKeyWithFactory<T, in THIS>(registry: KeyRegistry, name: String, val factory: THIS.() -> T) : RegistedKey<T>(registry, name)

abstract class KeyProvider<T>(val registry: KeyRegistry) {
    private val callback: MutableSet<(RegistedKey<T>) -> Unit> = ObjectArraySet()

    fun addCallback(action: (RegistedKey<T>) -> Unit) {
        callback += action
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <K : RegistedKey<T>> register(name: String, block: () -> K): K {
        return registry.keys.computeIfAbsent(name) {
            val key = block()
            callback.forEach { it(key) }
            callback.clear()
            key
        } as K
    }
}

fun <P : KeyProvider<T>, T> P.withCallback(action: (RegistedKey<T>) -> Unit) = apply { addCallback(action) }

fun <P : KeyProvider<T>, T> P.withSync() = withCallback { key -> if (registry is KeyRegistryWithSync) registry.keysToSync[key.name] = key }

interface KeyProviders {
    class Normal<T>(registry: KeyRegistry) : KeyProvider<T>(registry) {
        fun getKey(shortName: String): RegistedKey<T> {
            val name = registry.getKeyName(shortName)
            return register(name) { RegistedKey(registry, name) }
        }
    }

    class WithDefault<T>(registry: KeyRegistry, val defaultValue: T) : KeyProvider<T>(registry) {
        fun getKey(shortName: String): RegistedKeyWithDefault<T> {
            val name = registry.getKeyName(shortName)
            return register(name) { RegistedKeyWithDefault(registry, name, defaultValue) }
        }
    }

    class WithFactory<T, THIS>(registry: KeyRegistry, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
        fun getKey(shortName: String): RegistedKeyWithFactory<T, THIS> {
            val name = registry.getKeyName(shortName)
            return register(name) { RegistedKeyWithFactory(registry, name, factory) }
        }
    }

    class Named<T>(registry: KeyRegistry, val name: String) : KeyProvider<T>(registry) {
        fun getKey(): RegistedKey<T> {
            return register(name) { RegistedKey(registry, name) }
        }
    }

    class NamedWithDefault<T>(registry: KeyRegistry, val name: String, val defaultValue: T) : KeyProvider<T>(registry) {
        fun getKey(): RegistedKeyWithDefault<T> {
            return register(name) { RegistedKeyWithDefault(registry, name, defaultValue) }
        }
    }

    class NamedWithFactory<T, THIS>(registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>(registry) {
        fun getKey(): RegistedKeyWithFactory<T, THIS> {
            return register(name) { RegistedKeyWithFactory(registry, name, factory) }
        }
    }
}

inline fun <T> registerKey(registry: KeyRegistry): KeyProviders.Normal<T> = KeyProviders.Normal(registry)

inline fun <T> registerKey(registry: KeyRegistry, default: T): KeyProviders.WithDefault<T> = KeyProviders.WithDefault(registry, default)

inline fun <T, THIS> registerKey(registry: KeyRegistry, noinline factory: THIS.() -> T): KeyProviders.WithFactory<T, THIS> = KeyProviders.WithFactory(registry, factory)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String): KeyProviders.Named<T> = KeyProviders.Named(registry, name)

inline fun <T> registerNamedKey(registry: KeyRegistry, name: String, default: T): KeyProviders.NamedWithDefault<T> = KeyProviders.NamedWithDefault(registry, name, default)

inline fun <T, THIS> registerNamedKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T): KeyProviders.NamedWithFactory<T, THIS> = KeyProviders.NamedWithFactory(registry, name, factory)

inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKey<T> = getKey(property.name)

inline operator fun <T> KeyProviders.WithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithDefault<T> = getKey(property.name)

inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithFactory<T, THIS> = getKey(property.name)

inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKey<T> = getKey()

inline operator fun <T> KeyProviders.NamedWithDefault<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithDefault<T> = getKey()

inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithFactory<T, THIS> = getKey()

inline operator fun <T, K : RegistedKey<T>> K.getValue(thisRef: KeyRegistry, property: KProperty<*>): K = this
