@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.CaffeineSpec
import com.github.benmanes.caffeine.cache.LoadingCache
import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.base.ChronicleCapacities

/**
 * 使用 [Caffeine](https://github.com/ben-manes/caffeine) 构建缓存。
 *
 * @see Caffeine
 * @see CaffeineSpec
 */
fun CacheBuilder(spec: String = ""): Caffeine<Any, Any> {
    val builder = if (spec.isEmpty()) Caffeine.newBuilder() else Caffeine.from(spec)
    if (ChronicleCapacities.recordCacheStats()) builder.recordStats()
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
inline fun <K : Any, V : Any> Cache<K, V>.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V> {
    return TrackingCache(this, modificationTrackerProvider)
}

/** 将载入缓存包装为追踪缓存（[TrackingLoadingCache]），基于 [ModificationTracker] 自动失效。 */
inline fun <K : Any, V : Any> LoadingCache<K, V>.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingLoadingCache<K, V> {
    return TrackingLoadingCache(this, modificationTrackerProvider)
}

/** 创建嵌套缓存（[NestedCache]），用于为每个"外层键"懒创建一个内部缓存。 */
inline fun <RK : Any, K : Any, V : Any> createNestedCache(noinline cacheProvider: () -> Cache<K, V>): NestedCache<RK, K, V> {
    return NestedCache(cacheProvider)
}

/** 创建嵌套载入缓存（[NestedLoadingCache]），用于为每个"外层键"懒创建一个内部缓存。 */
inline fun <RK : Any, K : Any, V : Any> createNestedLoadingCache( noinline  cacheProvider: () -> LoadingCache<K, V>): NestedLoadingCache<RK, K, V> {
    return NestedLoadingCache(cacheProvider)
}
