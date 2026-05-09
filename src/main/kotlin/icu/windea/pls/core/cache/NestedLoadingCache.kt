package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.LoadingCache
import java.util.concurrent.ConcurrentHashMap

/**
 * 嵌套载入缓存。通过 [cacheProvider] 获取需要的缓存对象。
 */
class NestedLoadingCache<RK : Any, K : Any, V : Any>(
    private val cacheProvider: () -> LoadingCache<K, V>
) {
    private val cacheMap = ConcurrentHashMap<RK, LoadingCache<K, V>>()

    operator fun get(key: RK): LoadingCache<K, V> {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
