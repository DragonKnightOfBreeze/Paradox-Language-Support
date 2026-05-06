package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import java.util.concurrent.ConcurrentHashMap

/**
 * 嵌套缓存。通过 [cacheProvider] 获取需要的缓存对象。
 */
class NestedCache<RK : Any, K : Any, V : Any>(
    private val cacheProvider: () -> Cache<K, V>
) {
    private val cacheMap = ConcurrentHashMap<RK, Cache<K, V>>()

    operator fun get(key: RK): Cache<K, V> {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
