package icu.windea.pls.core.util

import com.google.common.cache.*
import com.jetbrains.rd.util.*

class NestedCache<RK, K: Any, V: Any, C: Cache<K,V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()
    
    fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}

