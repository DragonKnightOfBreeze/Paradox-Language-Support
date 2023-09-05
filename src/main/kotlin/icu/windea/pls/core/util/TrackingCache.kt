package icu.windea.pls.core.util

import com.google.common.cache.*
import com.intellij.openapi.util.*
import java.util.concurrent.*

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

fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}