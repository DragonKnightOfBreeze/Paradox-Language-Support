@file:Suppress("unused")

package icu.windea.pls

import com.google.common.base.*
import com.google.common.cache.*

private const val maxCacheSize = 1000L

fun <K : Any, V> createCache(): Cache<K, V> {
	return CacheBuilder.newBuilder().build()
}

fun <K : Any, V> createLimitedCache(): Cache<K, V> {
	return CacheBuilder.newBuilder().maximumSize(maxCacheSize).build()
}

fun <K : Any, V> createCache(builder: (K) -> V): LoadingCache<K, V> {
	return CacheBuilder.newBuilder().build(CacheLoader.from(Function { builder(it) }))
}

fun <K : Any, V> createLimitedCache(builder: (K) -> V): LoadingCache<K, V> {
	return CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(CacheLoader.from(Function { builder(it) }))
}

inline fun <K:Any, V> Cache<K, V>.getOrPut(key: K,crossinline defaultValue: () ->V):V{
	return asMap().computeIfAbsent(key) { defaultValue() }
}

operator fun <K : Any, V> Cache<K, V>.get(key: K): V? {
	return getIfPresent(key)
}

operator fun <K : Any, V> Cache<K, V>.set(key: K, value: V) {
	put(key, value)
}