@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.google.common.cache.*
import icu.windea.pls.core.*
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

inline fun <K : Any, V : Any> Cache<K, V>.getCancelable(key: K, loader: Callable<out V>): V {
    return cancelable { get(key, loader) }
}

inline fun <K : Any, V : Any> Cache<K, V>.getCancelable(key: K, defaultValueOnException: (Throwable) -> V, loader: Callable<out V>): V {
    return cancelable(defaultValueOnException) { get(key, loader) }
}

inline fun <K : Any, V : Any> LoadingCache<K, V>.getCancelable(key: K): V {
    return cancelable { get(key) }
}

