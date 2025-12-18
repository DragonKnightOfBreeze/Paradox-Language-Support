@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.CaffeineSpec
import com.github.benmanes.caffeine.cache.LoadingCache
import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.PlsFacade

/**
 * 使用 [Caffeine](https://github.com/ben-manes/caffeine) 构建缓存。
 *
 * @see Caffeine
 * @see CaffeineSpec
 */
fun CacheBuilder(spec: String = ""): Caffeine<Any, Any> {
    val builder = if (spec.isEmpty()) Caffeine.newBuilder() else Caffeine.from(spec)
    if (PlsFacade.Capacities.recordCacheStats()) builder.recordStats()
    return builder
}

/** 将缓存包装为可取消的缓存（[CancelableCache]）。 */
inline fun <K : Any, V : Any> Cache<K, V>.cancelable(): CancelableCache<K, V> {
    return CancelableCache(this)
}

/** 将载入缓存包装为可取消的缓存（[CancelableLoadingCache]）。 */
inline fun <K : Any, V : Any> LoadingCache<K, V>.cancelable(): CancelableLoadingCache<K, V> {
    return CancelableLoadingCache(this)
}

/** 将缓存包装为追踪缓存（[TrackingCache]），基于 [ModificationTracker] 自动失效。 */
inline fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}

/** 创建嵌套缓存（[NestedCache]），用于为每个“外层键”懒创建一个内部缓存。 */
inline fun <RK : Any, K : Any, V : Any, C : Cache<K, V>> createNestedCache(noinline cacheProvider: () -> C): NestedCache<RK, K, V, C> {
    return NestedCache(cacheProvider)
}

