@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.EMPTY_OBJECT
import kotlin.reflect.KProperty

// UserDataHolder

/**
 * 获取用户数据，如果不存在则通过 [action] 计算并保存。
 *
 * 兼容计算后的值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T & Any>, action: () -> T): T {
    val value = getUserData(key)
    if (value === EMPTY_OBJECT) return null as T
    if (value != null) return value
    val computed = action()
    // default value is still saved if it's null
    if(this is UserDataHolderEx) {
        putUserDataIfAbsent(key as Key<Any>, computed ?: EMPTY_OBJECT)
    } else {
        putUserData(key as Key<Any>, computed ?: EMPTY_OBJECT)
    }
    return computed
}

/**
 * 获取用户数据，如果不存在则尝试从 [key] 获取默认值或计算并保存。
 *
 * 兼容计算后的值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getOrPutUserData(key: Key<T>): T? {
    val value = getUserData(key)
    if (value === EMPTY_OBJECT) return null
    if (value != null) return value
    if (key is KeyWithDefault) return key.default
    if (key !is KeyWithFactory<*, *>) return null
    key as KeyWithFactory<T, THIS>
    val computed = key.factory(this)
    // default value is still saved if it's null
    if(this is UserDataHolderEx) {
        putUserDataIfAbsent(key as Key<Any>, computed ?: EMPTY_OBJECT)
    } else {
        putUserData(key as Key<Any>, computed ?: EMPTY_OBJECT)
    }
    return computed
}

/**
 * 获取用户数据，如果不存在则尝试从 [key] 获取默认值并保存。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getOrPutUserData(key: KeyWithDefault<T>): T {
    val value = getUserData(key)
    if (value === EMPTY_OBJECT) return key.default
    if (value != null) return value
    return key.default
}

/**
 * 获取用户数据，如果不存在则从 [key] 计算并保存。
 *
 * 兼容计算后的值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getOrPutUserData(key: KeyWithFactory<T, THIS>): T {
    val value = getUserData(key)
    if (value === EMPTY_OBJECT) return null as T
    if (value != null) return value
    val computed = key.factory(this)
    // default value is still saved if it's null
    if(this is UserDataHolderEx) {
        putUserDataIfAbsent(key as Key<Any>, computed ?: EMPTY_OBJECT)
    } else {
        putUserData(key as Key<Any>, computed ?: EMPTY_OBJECT)
    }
    return computed
}

inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? = thisRef.getOrPutUserData(this)

inline operator fun <T> KeyWithDefault<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T = thisRef.getOrPutUserData(this)

inline operator fun <T, THIS : UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T = thisRef.getOrPutUserData(this)

inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) = thisRef.putUserData(this, value)

// ProcessingContext

/**
 * 获取上下文数据，如果不存在则尝试从 [key] 获取默认值或计算并保存。
 *
 * 兼容计算后的值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrPut(key: Key<T>): T? {
    val value = get(key)
    if (value === EMPTY_OBJECT) return null
    if (value != null) return value
    if (key is KeyWithDefault) return key.default
    if (key !is KeyWithFactory<*, *>) return null
    key as KeyWithFactory<T, ProcessingContext>
    val computed = key.factory(this)
    // default value is still saved if it's null
    put(key as Key<Any>, computed ?: EMPTY_OBJECT)
    return computed
}

/**
 * 获取上下文数据，如果不存在则尝试从 [key] 获取默认值并保存。
 */
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrPut(key: KeyWithDefault<T>): T {
    val value = get(key)
    if (value === EMPTY_OBJECT) return key.default
    if (value != null) return value
    return key.default
}

/**
 * 获取上下文数据，如果不存在则从 [key] 计算并保存。
 *
 * 兼容计算后的值为 `null` 的情况，此时使用 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrPut(key: KeyWithFactory<T, ProcessingContext>): T {
    val value = get(key)
    if (value === EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
    // default value is still saved if it's null
    put(key as Key<Any>, defaultValue ?: EMPTY_OBJECT)
    return defaultValue
}

inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? = thisRef.getOrPut(this)

inline operator fun <T> KeyWithDefault<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T = thisRef.getOrPut(this)

inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T = thisRef.getOrPut(this)

inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) = thisRef.put(this, value)
