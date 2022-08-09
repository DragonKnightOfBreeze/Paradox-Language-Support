@file:Suppress("unused")

package icu.windea.pls

import com.google.common.cache.*

inline fun <K,V, K1: K, V1:V> CacheBuilder<K,V>.buildFrom(crossinline builder: (K1) -> V1): LoadingCache<K1, V1> {
	return build(object: CacheLoader<K1,V1>(){
		override fun load(key: K1): V1 {
			return builder(key)
		}
	})
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