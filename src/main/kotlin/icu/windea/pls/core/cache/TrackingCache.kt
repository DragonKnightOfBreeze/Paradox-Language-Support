package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.openapi.util.ModificationTracker
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * 追踪缓存。通过 [modificationTrackerProvider] 获取用于追踪值的更改的 [ModificationTracker]。
 *
 * @see ModificationTracker
 */
@Suppress("AddVarianceModifier")
class TrackingCache<K : Any, V : Any, in C : Cache<K, V>>(
    private val delegate: C,
    private val modificationTrackerProvider: (V) -> ModificationTracker?
) : Cache<K, V> by delegate {
    private val modificationCounts: MutableMap<K, Long> = ConcurrentHashMap()

    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        val result = delegate.get(key, mappingFunction)
        val modificationTracker = modificationTrackerProvider(result)
        if (modificationTracker == null || modificationTracker == ModificationTracker.NEVER_CHANGED) return result
        val newModificationCount = modificationTracker.modificationCount
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
