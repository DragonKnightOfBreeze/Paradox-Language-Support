@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.cast
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

/**
 * 带工厂方法的 Key。
 *
 * 适用于需要按需（惰性）创建默认值的场景，工厂接收具体的 `THIS` 容器实例。
 */
class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T) : Key<T>(name)

/** 创建普通的 [Key]。*/
inline fun <T> createKey(name: String): Key<T> = Key.create<T>(name)

/** 创建带工厂的 [KeyWithFactory]。*/
inline fun <T, THIS> createKey(name: String, noinline factory: THIS.() -> T): KeyWithFactory<T, THIS> = KeyWithFactory(name, factory)

/**
 * 键注册表。
 *
 * 以“类名.属性名”为约定生成 Key 名，并缓存创建过的 Key，避免重复实例化。
 */
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

/**
 * 可同步的键注册表。
 *
 * 记录需要“同步”的 Key 集合，可在不同的 [UserDataHolder] 之间复制这些键的数据。
 */
abstract class SyncedKeyRegistry : KeyRegistry() {
    val keysToSync: MutableSet<Key<*>> = mutableSetOf()

    //use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
    fun syncUserData(from: UserDataHolder, to: UserDataHolder) {
        keysToSync.forEach { key ->
            @Suppress("UNCHECKED_CAST")
            key as Key<Any>
            to.putUserData(key, from.getUserData(key))
        }
    }

    fun <T : KeyProvider<*>> T.synced() = apply { callback { keysToSync += it } }
}

/**
 * Key 提供器基类：允许在创建 Key 时注入回调（如加入同步列表）。
 */
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
    /** 基于注册表 + 属性名自动生成 Key 的提供器。*/
    class Normal<T>(val registry: KeyRegistry) : KeyProvider<T>() {
        fun getKey(propName: String): Key<T> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    /** 基于注册表 + 指定名称创建 Key 的提供器。*/
    class Named<T>(val registry: KeyRegistry, val name: String) : KeyProvider<T>() {
        fun getKey(): Key<T> {
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    /** 基于注册表 + 属性名创建带工厂的 Key 提供器。*/
    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(propName: String): KeyWithFactory<T, THIS> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }

    /** 基于注册表 + 指定名称创建带工厂的 Key 提供器。*/
    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(): KeyWithFactory<T, THIS> {
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }
}

/** 通过 `val key by createKey(registry)` 的方式惰性创建并缓存 Key。*/
inline fun <T> createKey(registry: KeyRegistry): KeyProviders.Normal<T> = KeyProviders.Normal(registry)

/** 通过 `val key by createKey(registry, name)` 的方式创建命名 Key。*/
inline fun <T> createKey(registry: KeyRegistry, name: String): KeyProviders.Named<T> = KeyProviders.Named(registry, name)

/** 通过 `val key by createKey(registry) { ... }` 的方式创建带工厂的 Key。*/
inline fun <T, THIS> createKey(registry: KeyRegistry, noinline factory: THIS.() -> T): KeyProviders.WithFactory<T, THIS> = KeyProviders.WithFactory(registry, factory)

/** 通过 `val key by createKey(registry, name) { ... }` 的方式创建命名且带工厂的 Key。*/
inline fun <T, THIS> createKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T): KeyProviders.NamedWithFactory<T, THIS> = KeyProviders.NamedWithFactory(registry, name, factory)

/** 属性委托：按属性名生成并返回 Key。*/
inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): Key<T> = getKey(property.name)

/** 属性委托：返回命名 Key。*/
inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>): Key<T> = getKey()

/** 属性委托：按属性名生成并返回带工厂的 Key。*/
inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey(property.name)

/** 属性委托：返回命名且带工厂的 Key。*/
inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>): KeyWithFactory<T, THIS> = getKey()

/** 允许直接 `val k: Key<T> by registry.key` 形式将 Key 返回给属性。*/
inline operator fun <T, THIS : KeyRegistry, K : Key<T>> K.getValue(thisRef: THIS, property: KProperty<*>): K = this
