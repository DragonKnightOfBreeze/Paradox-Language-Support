@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.EMPTY_OBJECT
import kotlin.reflect.KProperty

/**
 * 获取用户数据，如果不存在则通过 [action] 获取默认值并保存。
 * 兼容默认值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T & Any>, action: () -> T): T {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = action()
    // default value is still saved if it's null
    putUserData(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取用户数据，如果不存在则尝试从 [key] 获取默认值并保存。
 * 兼容默认值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
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
    // default value is still saved if it's null
    putUserData(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取用户数据，如果不存在则从 [key] 获取默认值并保存。
 * 兼容默认值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: RegistedKeyWithFactory<T, THIS>): T {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
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
    thisRef.putUserData(this, value)
}

/**
 * 获取用户数据，如果不存在则尝试从 [key] 获取默认值并保存。
 * 兼容默认值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: RegistedKey<T>): T? {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null
    if (value != null) return value
    val defaultValue = when {
        key is RegistedKeyWithFactory<*, *> -> (key as RegistedKeyWithFactory<T, ProcessingContext>).factory(this)
        else -> return null
    }
    // default value is still saved if it's null
    put(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取用户数据，如果不存在则从 [key] 获取默认值并保存。
 * 兼容默认值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: RegistedKeyWithFactory<T, ProcessingContext>): T {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
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
    thisRef.put(this, value)
}
