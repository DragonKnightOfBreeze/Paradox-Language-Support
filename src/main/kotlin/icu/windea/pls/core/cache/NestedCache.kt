package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import java.util.concurrent.ConcurrentHashMap

/**
 * 嵌套缓存。通过 [cacheProvider] 获取需要的缓存对象。
 */
@Suppress("AddVarianceModifier")
class NestedCache<RK : Any, K : Any, V : Any, C : Cache<K, V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()

    operator fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
