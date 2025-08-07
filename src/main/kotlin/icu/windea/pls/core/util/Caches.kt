@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.google.common.cache.*
import com.google.common.collect.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import java.util.concurrent.*

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return build<K, V>().toCancelable()
}

inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return build(object : CacheLoader<K, V>() {
        override fun load(key: K) = loader(key)
    }).toCancelable()
}

inline fun <K : Any, V : Any> CacheBuilder<in K, Any>.buildValueNullableCache(crossinline loader: (K) -> V?): ValueNullableLoadingCache<K, V> {
    return build(object : CacheLoader<K, Any>() {
        override fun load(key: K) = loader(key) ?: EMPTY_OBJECT
    }).toCancelable().toValueNullable<K, V>()
}

inline fun <K : Any, V : Any> Cache<K, V>.toCancelable(): CancelableCache<K, V> {
    return CancelableCache(this)
}

inline fun <K : Any, V : Any> LoadingCache<K, V>.toCancelable(): CancelableLoadingCache<K, V> {
    return CancelableLoadingCache(this)
}

inline fun <K : Any, V : Any> LoadingCache<K, Any>.toValueNullable(): ValueNullableLoadingCache<K, V> {
    return ValueNullableLoadingCache(this)
}

inline fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}

inline fun <RK : Any, K : Any, V : Any, C : Cache<K, V>> createNestedCache(noinline cacheProvider: () -> C): NestedCache<RK, K, V, C> {
    return NestedCache(cacheProvider)
}

class CancelableCache<K : Any, V : Any>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
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

class CancelableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, V>
) : LoadingCache<K, V> by delegate {
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

@Suppress("UNCHECKED_CAST")
class ValueNullableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, Any>
) : LoadingCache<K, Any> by delegate {
    fun getOrNull(key: K): V? {
        return get(key) as? V
    }

    fun getUncheckedOrNull(key: K): V? {
        return getUnchecked(key) as? V
    }
}

class TrackingCache<K : Any, V : Any, C : Cache<K, V>>(
    private val delegate: C,
    private val modificationTrackerProvider: (V) -> ModificationTracker?
) : Cache<K, V> by delegate {
    private val modificationCounts = hashMapOf<K, Long>()

    override fun get(key: K, loader: Callable<out V>): V {
        val result = delegate.get(key, loader)
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        modificationCounts.put(key, newModificationCount)
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        val newResult = delegate.get(key, loader)
        return newResult
    }

    override fun getIfPresent(key: Any): V? {
        val result = delegate.getIfPresent(key) ?: return null
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        return null
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

class NestedCache<RK : Any, K : Any, V : Any, C : Cache<K, V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()

    fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
