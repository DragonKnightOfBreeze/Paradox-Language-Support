@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.google.common.cache.*
import com.google.common.util.concurrent.*
import com.intellij.openapi.progress.*
import java.util.concurrent.*

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return build()
}

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return build(object : CacheLoader<K, V>() {
        override fun load(key: K): V {
            return loader(key)
        }
    })
}

inline fun <K : Any, V : Any> Cache<K, V>.getOrPut(key: K, crossinline defaultValue: (K) -> V): V {
    try {
        return get(key) { defaultValue(key) }
    } catch(e: ExecutionException) {
        val cause = e.cause
        if(cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch(e: UncheckedExecutionException) {
        val cause = e.cause
        if(cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch(e: ProcessCanceledException) {
        throw e
    }
}

inline fun <K : Any, V : Any> Cache<K, V>.getOrPut(key: K, defaultValueOnException: (Throwable) -> V, crossinline defaultValue: (K) -> V): V {
    try {
        return get(key) { defaultValue(key) }
    } catch(e: ExecutionException) {
        val cause = e.cause
        if(cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch(e: UncheckedExecutionException) {
        val cause = e.cause
        if(cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch(e: ProcessCanceledException) {
        throw e
    }
}

inline operator fun <K : Any, V : Any> Cache<K, V>.get(key: K): V? {
    return getIfPresent(key)
}

inline operator fun <K : Any, V : Any> Cache<K, V>.set(key: K, value: V) {
    put(key, value)
}
