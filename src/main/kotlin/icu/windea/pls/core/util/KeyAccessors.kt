@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.openapi.util.UserDataHolder
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.runCatchingCancelable
import kotlin.reflect.KProperty

/** 安全写入用户数据：出现可取消异常时自动吞掉。*/
inline fun <T> UserDataHolder.tryPutUserData(key: Key<T>, value: T?) {
    runCatchingCancelable { putUserData(key, value) }
}

/**
 * 获取或初始化用户数据。
 *
 * - 若不存在则调用 [action] 计算默认值并缓存；
 * - 若默认值为 `null`，以 [EMPTY_OBJECT] 占位存储，后续读取仍返回 `null`。
 */
@Suppress("UNCHECKED_CAST")
inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T & Any>, action: () -> T): T {
    val data = this.getUserData(key)
    if (data == EMPTY_OBJECT) return null as T
    if (data != null) return data
    val defaultValue = action()
    // default value is still saved if it's null
    if (defaultValue != null) putUserData(key, defaultValue) else putUserData(key as Key<Any>, EMPTY_OBJECT)
    return defaultValue
}

/**
 * 获取用户数据，若不存在则根据 [Key] 类型提供默认值：
 * - [KeyWithDefaultValue]：使用其 `defaultValue`；
 * - [KeyWithFactory]：调用其 `factory(this)`；
 * - 否则返回 `null`。
 *
 * 默认值为 `null` 时，同样以 [EMPTY_OBJECT] 占位存储。
 */
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: Key<T>): T? {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null
    if (value != null) return value
    val defaultValue = when {
        key is KeyWithDefaultValue -> key.defaultValue
        key is KeyWithFactory<*, *> -> (key as KeyWithFactory<T, THIS>).factory(this)
        else -> return null
    }
    // default value is still saved if it's null
    if (defaultValue != null) putUserData(key, defaultValue) else putUserData(key as Key<Any>, EMPTY_OBJECT)
    return defaultValue
}

/** 获取或初始化带工厂的 Key 的值，始终返回非空。*/
@Suppress("UNCHECKED_CAST")
fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: KeyWithFactory<T, THIS>): T {
    val value = getUserData(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
    // default value is still saved if it's null
    if (defaultValue != null) putUserData(key, defaultValue) else putUserData(key as Key<Any>, EMPTY_OBJECT)
    return defaultValue
}

/** 属性委托：`val v by Key<T>`，从 [UserDataHolder] 读取（含默认值逻辑）。*/
inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? {
    return thisRef.getUserDataOrDefault(this)
}

/** 属性委托：`val v by KeyWithFactory<T>`，从 [UserDataHolder] 读取或用工厂初始化。*/
inline operator fun <T, THIS : UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserDataOrDefault(this)
}

/** 属性委托写入：`var v by Key<T>`。*/
inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) {
    thisRef.putUserData(this, value)
}

/** 从 [ProcessingContext] 获取或按 Key 类型提供默认值（逻辑同 UserDataHolder 版本）。*/
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: Key<T>): T? {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null
    if (value != null) return value
    val defaultValue = when {
        key is KeyWithDefaultValue -> key.defaultValue
        key is KeyWithFactory<*, *> -> (key as KeyWithFactory<T, ProcessingContext>).factory(this)
        else -> return null
    }
    // default value is still saved if it's null
    if (defaultValue != null) put(key, defaultValue) else put(key as Key<Any>, EMPTY_OBJECT)
    return defaultValue
}

/** 获取或初始化带工厂的 Key 的值（ProcessingContext 版本）。*/
@Suppress("UNCHECKED_CAST")
fun <T> ProcessingContext.getOrDefault(key: KeyWithFactory<T, ProcessingContext>): T {
    val value = get(key)
    if (value == EMPTY_OBJECT) return null as T
    if (value != null) return value
    val defaultValue = key.factory(this)
    // default value is still saved if it's null
    if (defaultValue != null) put(key, defaultValue) else put(key as Key<Any>, EMPTY_OBJECT)
    return defaultValue
}

/** 属性委托：从 [ProcessingContext] 读取。*/
inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? {
    return thisRef.getOrDefault(this)
}

/** 属性委托：从 [ProcessingContext] 读取或用工厂初始化。*/
inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.getOrDefault(this)
}

/** 属性委托写入（ProcessingContext 版本）。*/
inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) {
    thisRef.put(this, value)
}
