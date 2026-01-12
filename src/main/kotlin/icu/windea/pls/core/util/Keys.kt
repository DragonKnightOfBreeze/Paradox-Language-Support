@file:Suppress("NOTHING_TO_INLINE")
@file:Optimized

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.FastSet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

inline fun <T> createKey(name: String): Key<T> = Key.create<T>(name)

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

    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): RegistedKey<T>? {
        return keys.get(name) as? RegistedKey<T>
    }

    fun copy(from: UserDataHolder, to: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
        keys.values.forEach { key -> key.copy(from, to, ifPresent) }
    }

    fun <P : KeyProvider<T>, T> P.withCallback(action: (RegistedKey<T>) -> Unit) = apply { addCallback(action) }
}

abstract class KeyRegistryWithSync : KeyRegistry() {
    val keysToSync: MutableMap<String, RegistedKey<*>> = ConcurrentHashMap()

    fun sync(from: UserDataHolder, to: UserDataHolder, ifPresent: Boolean = false) {
        // use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
        keysToSync.values.forEach { key -> key.copy(from, to, ifPresent) }
    }

    fun <P : KeyProvider<T>, T> P.synced() = withCallback { key -> keysToSync[key.name] = key }
}

open class RegistedKey<T>(val registry: KeyRegistry, val name: String) : Key<T>(name)

class RegistedKeyWithFactory<T, in THIS>(registry: KeyRegistry, name: String, val factory: THIS.() -> T) : RegistedKey<T>(registry, name)

abstract class KeyProvider<T> {
    private val callback: FastSet<(RegistedKey<T>) -> Unit> = FastSet()

    fun addCallback(action: (RegistedKey<T>) -> Unit) {
        callback += action
    }

    protected fun getKeyName(registry: KeyRegistry, propName: String): String {
        return "${registry.id}.${propName}"
    }

    protected fun finish(key: RegistedKey<T>): RegistedKey<T> {
        callback.forEach { it(key) }
        callback.clear()
        return key
    }
}

interface KeyProviders {
    class Normal<T>(val registry: KeyRegistry) : KeyProvider<T>() {
        fun getKey(propName: String): RegistedKey<T> {
            val name = getKeyName(registry, propName)
            return registry.keys.getOrPut(name) { finish(RegistedKey(registry, name)) }.cast()
        }
    }

    class Named<T>(val registry: KeyRegistry, val name: String) : KeyProvider<T>() {
        fun getKey(): RegistedKey<T> {
            return registry.keys.getOrPut(name) { finish(RegistedKey(registry, name)) }.cast()
        }
    }

    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(propName: String): RegistedKeyWithFactory<T, THIS> {
            val name = getKeyName(registry, propName)
            return registry.keys.getOrPut(name) { finish(RegistedKeyWithFactory(registry, name, factory)) }.cast()
        }
    }

    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(): RegistedKeyWithFactory<T, THIS> {
            return registry.keys.getOrPut(name) { finish(RegistedKeyWithFactory(registry, name, factory)) }.cast()
        }
    }
}

inline fun <T> registerKey(registry: KeyRegistry): KeyProviders.Normal<T> = KeyProviders.Normal(registry)

inline fun <T> registerKey(registry: KeyRegistry, name: String): KeyProviders.Named<T> = KeyProviders.Named(registry, name)

inline fun <T, THIS> registerKey(registry: KeyRegistry, noinline factory: THIS.() -> T): KeyProviders.WithFactory<T, THIS> = KeyProviders.WithFactory(registry, factory)

inline fun <T, THIS> registerKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T): KeyProviders.NamedWithFactory<T, THIS> = KeyProviders.NamedWithFactory(registry, name, factory)

inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKey<T> = getKey(property.name)

inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKey<T> = getKey()

inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithFactory<T, THIS> = getKey(property.name)

inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): RegistedKeyWithFactory<T, THIS> = getKey()

inline operator fun <T, THIS : KeyRegistry, K : RegistedKey<T>> K.getValue(thisRef: THIS, property: KProperty<*>): K = this
