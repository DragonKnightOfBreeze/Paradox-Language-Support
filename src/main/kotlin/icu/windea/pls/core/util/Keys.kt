@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import icu.windea.pls.core.cast
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

/** 带默认值工厂的 Key。 */
class KeyWithFactory<T, in THIS>(name: String, val factory: THIS.() -> T) : Key<T>(name)

/** 创建一个简单的 Key。 */
inline fun <T> createKey(name: String): Key<T> = Key.create<T>(name)

/** 创建一个带工厂的 Key。 */
inline fun <T, THIS> createKey(name: String, noinline factory: THIS.() -> T): KeyWithFactory<T, THIS> = KeyWithFactory(name, factory)

/** 用于统一管理 Key 的注册表基类。 */
abstract class KeyRegistry {
    /** 注册表 ID（基于类名派生）。 */
    val id = javaClass.name.substringAfterLast(".").replace("Keys", "")
    /** 已注册 Key 的缓存。 */
    val keys: MutableMap<String, Key<*>> = ConcurrentHashMap()

    /** 根据属性名生成 Key 的全名。 */
    fun getKeyName(propName: String): String {
        return "${id}.${propName}"
    }

    @Suppress("UNCHECKED_CAST")
    /** 按名称获取已注册的 Key。 */
    fun <T> getKey(name: String): Key<T>? {
        return keys.get(name) as? Key<T>
    }
}

/** Key 提供器基类，支持注册时回调。 */
abstract class KeyProvider<T> {
    protected var callback: ((Key<T>) -> Unit)? = null

    protected fun Key<T>.runCallback(): Key<T> {
        callback?.invoke(this)
        return this
    }

    /** 设置 Key 创建完成时的回调。 */
    fun callback(action: (Key<T>) -> Unit) {
        this.callback = action
    }
}

interface KeyProviders {
    /** 基于属性名生成名称的 Key 提供器。 */
    class Normal<T>(val registry: KeyRegistry): KeyProvider<T>() {
        fun getKey(propName: String): Key<T> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    /** 使用给定名称的 Key 提供器。 */
    class Named<T>(val registry: KeyRegistry, val name: String): KeyProvider<T>() {
        fun getKey(): Key<T> {
            return registry.keys.getOrPut(name) { createKey<T>(name).runCallback() }.cast()
        }
    }

    /** 基于属性名生成名称且附带默认值工厂的 Key 提供器。 */
    class WithFactory<T, THIS>(val registry: KeyRegistry, val factory: THIS.() -> T): KeyProvider<T>() {
        fun getKey(propName: String): KeyWithFactory<T, THIS> {
            val name = registry.getKeyName(propName)
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }

    /** 使用给定名称且附带默认值工厂的 Key 提供器。 */
    class NamedWithFactory<T, THIS>(val registry: KeyRegistry, val name: String, val factory: THIS.() -> T) : KeyProvider<T>() {
        fun getKey(): KeyWithFactory<T, THIS> {
            return registry.keys.getOrPut(name) { createKey(name, factory).runCallback() }.cast()
        }
    }
}

/** 基于注册表创建按属性名命名的 Key 提供器。 */
inline fun <T> createKey(registry: KeyRegistry) = KeyProviders.Normal<T>(registry)

/** 基于注册表与名称创建命名 Key 提供器。 */
inline fun <T> createKey(registry: KeyRegistry, name: String) = KeyProviders.Named<T>(registry, name)

/** 基于注册表创建带工厂的 Key 提供器。 */
inline fun <T, THIS> createKey(registry: KeyRegistry, noinline factory: THIS.() -> T) = KeyProviders.WithFactory(registry, factory)

/** 基于注册表与名称创建带工厂的命名 Key 提供器。 */
inline fun <T, THIS> createKey(registry: KeyRegistry, name: String, noinline factory: THIS.() -> T) = KeyProviders.NamedWithFactory(registry, name, factory)

/** 属性委托：使用属性名注册/获取 Key。 */
inline operator fun <T> KeyProviders.Normal<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

/** 属性委托：使用固定名称注册/获取 Key。 */
inline operator fun <T> KeyProviders.Named<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

/** 属性委托：使用属性名注册/获取带工厂的 Key。 */
inline operator fun <T, THIS> KeyProviders.WithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey(property.name)

/** 属性委托：使用固定名称注册/获取带工厂的 Key。 */
inline operator fun <T, THIS> KeyProviders.NamedWithFactory<T, THIS>.provideDelegate(thisRef: Any?, property: KProperty<*>) = getKey()

/** 在 `KeyRegistry` 上以委托形式直接获取 Key 本身。 */
inline operator fun <T, THIS : KeyRegistry> Key<T>.getValue(thisRef: THIS, property: KProperty<*>) = this
