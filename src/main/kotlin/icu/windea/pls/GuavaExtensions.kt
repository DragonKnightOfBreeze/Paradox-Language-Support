@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls

import com.google.common.cache.*
import java.util.concurrent.*

private const val debugMode = false

fun <K,V> CacheBuilder<K,V>.withDebugMode(): CacheBuilder<K, V> {
	if(debugMode) return maximumSize(0)
	return this
}

inline fun <K, V, K1 : K, V1 : V> CacheBuilder<K, V>.buildCache(): Cache<K1, V1> {
	return withDebugMode().build()
}

inline fun <K, V, K1 : K, V1 : V> CacheBuilder<K, V>.buildCache(crossinline builder: (K1) -> V1): LoadingCache<K1, V1> {
	return withDebugMode().build(object : CacheLoader<K1, V1>() {
		override fun load(key: K1): V1 {
			return builder(key)
		}
	})
}

inline fun <K : Any, V> Cache<K, V>.getOrPut(key: K, crossinline defaultValue: () -> V): V {
	try {
		return get(key) { defaultValue() }
	} catch(e: ExecutionException) {
		throw e.cause ?: e
	}
}

operator fun <K : Any, V> Cache<K, V>.get(key: K): V? {
	return getIfPresent(key)
}

operator fun <K : Any, V> Cache<K, V>.set(key: K, value: V) {
	put(key, value)
}