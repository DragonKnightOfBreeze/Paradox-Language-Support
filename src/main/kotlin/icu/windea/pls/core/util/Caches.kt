@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

import com.google.common.cache.*
import com.google.common.collect.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import java.util.concurrent.*

//region Extensions

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return build<K,V>().toCancelable()
}

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return build(object : CacheLoader<K, V>() {
        override fun load(key: K): V {
            return loader(key)
        }
    }).toCancelable()
}

//endregion

//region CancelableCache

class CancelableCache<K: Any, V: Any>(
    private val delegate: Cache<K, V>
): Cache<K,V> by delegate {
    override fun get(key: K, loader: Callable<out V>): V {
        return cancelable { delegate.get(key, loader) }
    }
    
    override fun getIfPresent(key: Any): V? {
        return cancelable { delegate.getIfPresent(key) }
    }
    
    override fun getAllPresent(keys: MutableIterable<Any>): ImmutableMap<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }
}

class CancelableLoadingCache<K: Any, V: Any>(
    private val delegate: LoadingCache<K, V>
): LoadingCache<K,V> by delegate {
    override fun get(key: K, loader: Callable<out V>): V {
        return cancelable { delegate.get(key, loader) }
    }
    
    override fun getIfPresent(key: Any): V? {
        return cancelable { delegate.getIfPresent(key) }
    }
    
    override fun getAllPresent(keys: MutableIterable<Any>): ImmutableMap<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }
    
    override fun get(key: K): V {
        return cancelable { delegate.get(key) }
    }
    
    override fun getUnchecked(key: K): V {
        return cancelable { delegate.getUnchecked(key) }
    }
    
    override fun getAll(keys: MutableIterable<K>): ImmutableMap<K, V> {
        return cancelable { delegate.getAll(keys) }
    }
}

inline fun <K: Any,V: Any> Cache<K, V>.toCancelable(): CancelableCache<K,V> {
    return CancelableCache(this)
}

inline fun <K: Any,V: Any> LoadingCache<K, V>.toCancelable(): CancelableLoadingCache<K,V> {
    return CancelableLoadingCache(this)
}

//endregion

//region TrackingCache

class TrackingCache<K : Any, V : Any, C : Cache<K, V>>(
    private val delegate: C,
    private val modificationTrackerProvider: (V) -> ModificationTracker?
) : Cache<K, V> by delegate {
    private val modificationCounts = hashMapOf<K, Long>()
    
    override fun get(key: K, loader: Callable<out V>): V {
        val result = delegate.get(key, loader)
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if(newModificationCount == null || newModificationCount != modificationCounts.get(key)) {
            delegate.invalidate(key)
            val newResult = delegate.get(key, loader)
            return newResult
        }
        modificationCounts.put(key, newModificationCount)
        return result
    }
    
    override fun getIfPresent(key: Any): V? {
        val result = delegate.getIfPresent(key) ?: return null
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if(newModificationCount == null || newModificationCount != modificationCounts.get(key)) {
            return null
        }
        return result
        
    }
    
    override fun invalidate(key: Any) {
        modificationCounts.remove(key)
        delegate.invalidate(key)
    }
    
    override fun invalidateAll(keys: MutableIterable<Any>) {
        keys.forEach { modificationCounts.remove(it) }
        delegate.invalidateAll(keys)
    }
    
    override fun invalidateAll() {
        modificationCounts.clear()
        delegate.invalidateAll()
    }
}

inline fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}

//endregion

//region NestedCache

class NestedCache<RK, K: Any, V: Any, C: Cache<K,V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()
    
    fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}

inline fun <RK, K: Any, V: Any, C: Cache<K,V>> createNestedCache(noinline cacheProvider: () -> C): NestedCache<RK, K, V, C> {
    return NestedCache(cacheProvider)
}

//endregion