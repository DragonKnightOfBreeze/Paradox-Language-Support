@file:Suppress("NOTHING_TO_INLINE", "PackageDirectoryMismatch")

package icu.windea.pls.core

import com.google.common.cache.*
import com.google.common.util.concurrent.*
import java.util.concurrent.*
import java.util.function.Function

fun <K, V> CacheBuilder<K, V>.withDebugMode(): CacheBuilder<K, V> {
	if(PlsConstants.debugMode) return maximumSize(0)
	return this
}

inline fun <K, V> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
	return withDebugMode().build()
}

inline fun <K, V> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
	return withDebugMode().build(object : CacheLoader<K, V>() {
		override fun load(key: K): V {
			return loader(key)
		}
	})
}

inline fun <K, V> CacheBuilder<in K, in V>.buildLazyCache(): LoadingCache<K, V> {
	return LazyLoadingCache(this.withDebugMode(), LazyCacheLoader())
}

inline fun <K : Any, V> Cache<K, V>.getOrPut(key: K, crossinline defaultValue: (K) -> V): V {
	val lazyLoadingCache = this as? LazyLoadingCache<K,V>
	if(lazyLoadingCache != null) {
		if(lazyLoadingCache.loader.function == null) {
			synchronized(lazyLoadingCache.loader) {
				if(lazyLoadingCache.loader.function == null) {
					lazyLoadingCache.loader.function = Function { defaultValue(it) }
				}
			}
		}
	}
	try {
		if(lazyLoadingCache != null) {
			return lazyLoadingCache.getUnchecked(key)
		}
		return get(key) { defaultValue(key) }
	} catch(e: ExecutionException) {
		throw e.cause ?: e
	} catch(e: UncheckedExecutionException) {
		throw e.cause ?: e
	}
}

inline fun <K : Any, V> Cache<K, V>.getOrPut(key: K, defaultValueOnException: (Throwable) -> V, crossinline defaultValue: (K) -> V): V {
	try {
		return get(key) { defaultValue(key) }
	} catch(e: ExecutionException) {
		return defaultValueOnException(e.cause ?: e)
	} catch(e: UncheckedExecutionException) {
		return defaultValueOnException(e.cause ?: e)
	}
}

operator fun <K : Any, V> Cache<K, V>.get(key: K): V? {
	return getIfPresent(key)
}

operator fun <K : Any, V> Cache<K, V>.set(key: K, value: V) {
	put(key, value)
}

class LazyLoadingCache<K, V>(
	builder: CacheBuilder<in K, in V>,
	val loader: LazyCacheLoader<K, V>
) : LoadingCache<K, V> by builder.build(loader)

class LazyCacheLoader<K, V> : CacheLoader<K, V>() {
	var function: Function<K, V>? = null
	
	override fun load(key: K): V {
		return function?.apply(key) ?: throw IllegalStateException()
	}
}

