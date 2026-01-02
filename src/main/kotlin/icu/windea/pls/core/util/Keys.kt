@file:Optimized
@file:Suppress("NOTHING_TO_INLINE")

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
abstract class KeyRegistry {
    val id = javaClass.name.substringAfterLast(".").replace("\$Keys", "")
    val keys: MutableMap<String, RegistedKey<*>> = ConcurrentHashMap()

    fun getKeyName(propName: String): String {
        return "${id}.${propName}"
    }

    fun <T> getKey(name: String): RegistedKey<T>? {
        return keys.get(name) as? RegistedKey<T>
    }
}

abstract class SyncedKeyRegistry : KeyRegistry() {
    val syncedKeys: FastSet<RegistedKey<*>> = FastSet()

    @Suppress("UNCHECKED_CAST")
    fun syncUserData(from: UserDataHolder, to: UserDataHolder) {
        // use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
        syncedKeys.forEach { to.putUserData(it as RegistedKey<Any>, from.getUserData(it)) }
    }

    fun <T : KeyProvider<*>> T.synced() = apply { onCreated { syncedKeys.add(it) } }
}

open class RegistedKey<T>(val registry: KeyRegistry, name: String) : Key<T>(name) {
    val onWriteCallbacks: FastSet<(Any?, RegistedKey<T>) -> Unit> = FastSet()
}

class RegistedKeyWithFactory<T, in THIS>(registry: KeyRegistry, name: String, val factory: THIS.() -> T) : RegistedKey<T>(registry, name)

abstract class KeyProvider<T> {
    val onCreatedCallbacks: FastSet<(RegistedKey<T>) -> Unit> = FastSet()
    val onWriteCallbacks: FastSet<(Any?, RegistedKey<T>) -> Unit> = FastSet()

    protected fun finish(key: RegistedKey<T>): RegistedKey<T> {
        onCreatedCallbacks.forEach { it(key) }
        onCreatedCallbacks.clear()
        key.onWriteCallbacks += onWriteCallbacks
        onWriteCallbacks.clear()
        return key
    }

    fun onCreated(action: (RegistedKey<T>) -> Unit) {
        onCreatedCallbacks += action
    }

    fun onWrite(action: (Any?, RegistedKey<T>) -> Unit) {
        onWriteCallbacks += action
    }
}

interface KeyProviders {
    class Normal<T>(val registry: KeyRegistry) : KeyProvider<T>() {
        fun getKey(propName: String): RegistedKey<T> {
            val name = registry.getKeyName(propName)
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
            val name = registry.getKeyName(propName)
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
