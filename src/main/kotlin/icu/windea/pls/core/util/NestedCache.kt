package icu.windea.pls.core.util

import com.google.common.cache.Cache
import com.jetbrains.rd.util.*

class NestedCache<RK, K, V, C: Cache<K,V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()
    
    fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}