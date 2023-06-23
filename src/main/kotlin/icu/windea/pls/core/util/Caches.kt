@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.google.common.cache.*
import com.google.common.util.concurrent.*
import com.intellij.openapi.progress.*
import java.util.concurrent.*

inline fun <K, V> CacheBuilder<K, V>.configure(): CacheBuilder<K, V> {
    //return maximumSize(0)
    return this
}

inline fun <K, V> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return configure().build()
}

inline fun <K, V> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return configure().build(object : CacheLoader<K, V>() {
        override fun load(key: K): V {
            return loader(key)
        }
    })
}

inline fun <K : Any, V> Cache<K, V>.getOrPut(key: K, crossinline defaultValue: (K) -> V): V {
    try {
        return get(key) { defaultValue(key) }
    } catch(e: ExecutionException) {
        throw e.cause ?: e
    } catch(e: UncheckedExecutionException) {
        throw e.cause ?: e
    } catch(e: ProcessCanceledException) {
        throw e
    }
}

inline fun <K : Any, V> Cache<K, V>.getOrPut(key: K, defaultValueOnException: (Throwable) -> V, crossinline defaultValue: (K) -> V): V {
    try {
        return get(key) { defaultValue(key) }
    } catch(e: ExecutionException) {
        return defaultValueOnException(e.cause ?: e)
    } catch(e: UncheckedExecutionException) {
        return defaultValueOnException(e.cause ?: e)
    } catch(e: ProcessCanceledException) {
        throw e
    }
}

inline operator fun <K : Any, V> Cache<K, V>.get(key: K): V? {
    return getIfPresent(key)
}

inline operator fun <K : Any, V> Cache<K, V>.set(key: K, value: V) {
    put(key, value)
}
