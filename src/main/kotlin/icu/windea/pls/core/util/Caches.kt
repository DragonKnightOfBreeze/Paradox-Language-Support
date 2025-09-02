@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.CaffeineSpec
import com.github.benmanes.caffeine.cache.LoadingCache
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.core.cancelable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * 使用 [Caffeine](https://github.com/ben-manes/caffeine) 构建缓存。
 *
 * @see Caffeine
 * @see CaffeineSpec
 */
fun CacheBuilder(spec: String? = null): Caffeine<Any, Any> {
    if (spec.isNullOrEmpty()) return Caffeine.newBuilder()
    return Caffeine.from(spec)
}

/** 转换为可取消的缓存（[CancelableCache]）。 */
inline fun <K : Any, V : Any> Cache<K, V>.cancelable(): CancelableCache<K, V> {
    return CancelableCache(this)
}

/** 转换为可取消的载入缓存（[CancelableLoadingCache]）。 */
inline fun <K : Any, V : Any> LoadingCache<K, V>.cancelable(): CancelableLoadingCache<K, V> {
    return CancelableLoadingCache(this)
}

/** 转换为追踪缓存（[TrackingCache]）。 */
inline fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}

/** 创建嵌套缓存（[NestedCache]）。 */
inline fun <RK : Any, K : Any, V : Any, C : Cache<K, V>> createNestedCache(noinline cacheProvider: () -> C): NestedCache<RK, K, V, C> {
    return NestedCache(cacheProvider)
}

/**
 * 可取消的缓存。使用 [cancelable] 包装取值方法。
 *
 * @see ProcessCanceledException
 */
class CancelableCache<K : Any, V : Any>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        return cancelable { delegate.get(key, mappingFunction) }
    }

    override fun getIfPresent(key: K): V? {
        return cancelable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: Iterable<K>): Map<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }
}

/**
 * 可取消的载入缓存。使用 [cancelable] 包装取值方法。
 *
 * @see ProcessCanceledException
 */
class CancelableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, V>
) : LoadingCache<K, V> by delegate {
    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        return cancelable { delegate.get(key, mappingFunction) }
    }

    override fun getIfPresent(key: K): V? {
        return cancelable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: Iterable<K>): Map<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }

    override fun get(key: K): V {
        return cancelable { delegate.get(key) }
    }

    override fun getAll(keys: Iterable<K>): Map<K, V> {
        return cancelable { delegate.getAll(keys) }
    }
}

/**
 * 追踪缓存。通过 [modificationTrackerProvider] 获取用于追踪值的更改的 [ModificationTracker]。
 *
 * @see ModificationTracker
 */
class TrackingCache<K : Any, V : Any, C : Cache<K, V>>(
    private val delegate: C,
    private val modificationTrackerProvider: (V) -> ModificationTracker?
) : Cache<K, V> by delegate {
    private val modificationCounts: MutableMap<K, Long> = ConcurrentHashMap()

    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        val result = delegate.get(key, mappingFunction)
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        modificationCounts.put(key, newModificationCount)
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        val newResult = delegate.get(key, mappingFunction)
        return newResult
    }

    override fun getIfPresent(key: K): V? {
        val result = delegate.getIfPresent(key) ?: return null
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        return null
    }

    override fun invalidate(key: K) {
        modificationCounts.remove(key)
        delegate.invalidate(key)
    }

    override fun invalidateAll(keys: Iterable<K>) {
        keys.forEach { modificationCounts.remove(it) }
        delegate.invalidateAll(keys)
    }

    override fun invalidateAll() {
        modificationCounts.clear()
        delegate.invalidateAll()
    }
}

/**
 * 嵌套缓存。通过 [cacheProvider] 获取需要的缓存对象。
 */
class NestedCache<RK : Any, K : Any, V : Any, C : Cache<K, V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()

    operator fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
