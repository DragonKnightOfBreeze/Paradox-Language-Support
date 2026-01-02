@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.openapi.util.UserDataHolder
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.EMPTY_OBJECT
import kotlin.reflect.KProperty

/**
 * 获取或初始化用户数据。
 *
 * - 如果不存在则调用 [action] 计算默认值并缓存。
 * - 如果默认值为 `null`，以 [EMPTY_OBJECT] 占位存储，后续读取仍返回 `null`。
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T & Any>, action: () -> T): T {
    val data = this.getUserData(key)
    if (data == EMPTY_OBJECT) return null as T
    if (data != null) return data
    val defaultValue = action()
    // default value is still saved if it's null
    putUserData(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取用户数据，如果不存在则根据 [Key] 类型提供默认值：
 * - [KeyWithDefaultValue]：使用其 `defaultValue`；
 * - [RegistedKeyWithFactory]：调用其 `factory(this)`；
 * - 否则返回 `null`。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: RegistedKey<T>): T? {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null
    if (value != null) return value
    val defaultValue = when {
        key is RegistedKeyWithFactory<*, *> -> (key as RegistedKeyWithFactory<T, THIS>).factory(this)
        else -> return null
    }
    // run write callbacks here
    key.onWriteCallbacks.forEach { it(this, key) }
    // default value is still saved if it's null
    putUserData(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取或初始化带工厂的 Key 的值，始终返回非空。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: RegistedKeyWithFactory<T, THIS>): T {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
    // run write callbacks here
    key.onWriteCallbacks.forEach { it(this, key) }
    // default value is still saved if it's null
    putUserData(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

inline operator fun <T> RegistedKey<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? {
    return thisRef.getUserDataOrDefault(this)
}

inline operator fun <T, THIS : UserDataHolder> RegistedKeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserDataOrDefault(this)
}

inline operator fun <T> RegistedKey<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) {
    onWriteCallbacks.forEach { it(this, this) } // run write callbacks here
    thisRef.putUserData(this, value)
}

/** 从 [ProcessingContext] 获取或按 Key 类型提供默认值（逻辑同 UserDataHolder 版本）。*/
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: RegistedKey<T>): T? {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null
    if (value != null) return value
    val defaultValue = when {
        key is RegistedKeyWithFactory<*, *> -> (key as RegistedKeyWithFactory<T, ProcessingContext>).factory(this)
        else -> return null
    }
    // run write callbacks here
    key.onWriteCallbacks.forEach { it(this, key) }
    // default value is still saved if it's null
    put(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/** 获取或初始化带工厂的 Key 的值（ProcessingContext 版本）。*/
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: RegistedKeyWithFactory<T, ProcessingContext>): T {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
    // run write callbacks here
    key.onWriteCallbacks.forEach { it(this, key) }
    // default value is still saved if it's null
    put(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

inline operator fun <T> RegistedKey<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? {
    return thisRef.getOrDefault(this)
}

inline operator fun <T> RegistedKeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.getOrDefault(this)
}

inline operator fun <T> RegistedKey<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) {
    onWriteCallbacks.forEach { it(this, this) } // run write callbacks here
    thisRef.put(this, value)
}
