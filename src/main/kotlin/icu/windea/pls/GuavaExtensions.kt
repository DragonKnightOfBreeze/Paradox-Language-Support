@file:Suppress("unused")

package icu.windea.pls

import com.google.common.base.Function
import com.google.common.cache.*
import java.nio.file.*

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

//相比get(key, loader)，这个方法性能应当更高？
fun <K:Any, V> Cache<K, V>.getOrPut(key: K, defaultValue: () ->V):V{
	return asMap().getOrPut(key, defaultValue)
}

operator fun <K : Any, V> Cache<K, V>.get(key: K): V? {
	return getIfPresent(key)
}

operator fun <K : Any, V> Cache<K, V>.set(key: K, value: V) {
	put(key, value)
}