@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import kotlin.reflect.*

inline fun <T> UserDataHolder.tryPutUserData(key: Key<T>, value: T?) {
    runCatchingCancelable { putUserData(key, value) }
}

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

inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? {
    return thisRef.getUserDataOrDefault(this)
}

inline operator fun <T, THIS : UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserDataOrDefault(this)
}

inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) {
    thisRef.putUserData(this, value)
}

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

inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? {
    return thisRef.getOrDefault(this)
}

inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.getOrDefault(this)
}

inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) {
    thisRef.put(this, value)
}
