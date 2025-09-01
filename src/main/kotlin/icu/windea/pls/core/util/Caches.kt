@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.ImmutableMap
import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.cancelable
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

/** 使用 Guava CacheBuilder 构建普通缓存，并包装为可取消的缓存。 */
inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(): Cache<K, V> {
    return build<K, V>().toCancelable()
}

/** 使用 Guava CacheBuilder 构建带加载器的缓存，并包装为可取消的缓存。 */
inline fun <K : Any, V : Any> CacheBuilder<in K, in V>.buildCache(crossinline loader: (K) -> V): LoadingCache<K, V> {
    return build(object : CacheLoader<K, V>() {
        override fun load(key: K) = loader(key)
    }).toCancelable()
}

/** 使用 Guava CacheBuilder 构建允许值为 null 的加载缓存（以占位对象保存 null）。 */
inline fun <K : Any, V : Any> CacheBuilder<in K, Any>.buildValueNullableCache(crossinline loader: (K) -> V?): ValueNullableLoadingCache<K, V> {
    return build(object : CacheLoader<K, Any>() {
        override fun load(key: K) = loader(key) ?: EMPTY_OBJECT
    }).toCancelable().toValueNullable<K, V>()
}

/** 将缓存包装为可取消的缓存实现（在受控上下文中执行）。 */
inline fun <K : Any, V : Any> Cache<K, V>.toCancelable(): CancelableCache<K, V> {
    return CancelableCache(this)
}

/** 将加载缓存包装为可取消的缓存实现。 */
inline fun <K : Any, V : Any> LoadingCache<K, V>.toCancelable(): CancelableLoadingCache<K, V> {
    return CancelableLoadingCache(this)
}

/** 将值类型为 Any 的加载缓存视为可返回 null 的加载缓存。 */
inline fun <K : Any, V : Any> LoadingCache<K, Any>.toValueNullable(): ValueNullableLoadingCache<K, V> {
    return ValueNullableLoadingCache(this)
}

/**
 * 为缓存增加基于 ModificationTracker 的自动失效机制。
 * 当条目的修改计数变化时，将自动失效并在下一次访问时刷新。
 */
inline fun <K : Any, V : Any, C : Cache<K, V>> C.trackedBy(noinline modificationTrackerProvider: (V) -> ModificationTracker?): TrackingCache<K, V, C> {
    return TrackingCache(this, modificationTrackerProvider)
}

/** 创建按“外层键”分片维护多个缓存实例的嵌套缓存。 */
inline fun <RK : Any, K : Any, V : Any, C : Cache<K, V>> createNestedCache(noinline cacheProvider: () -> C): NestedCache<RK, K, V, C> {
    return NestedCache(cacheProvider)
}

/** 为 Cache 的常用方法增加取消支持的包装实现。 */
class CancelableCache<K : Any, V : Any>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
    override fun get(key: K, loader: Callable<out V>): V {
        return cancelable { delegate.get(key, loader) }
    }

    override fun getIfPresent(key: Any): V? {
        return cancelable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: MutableIterable<Any>): ImmutableMap<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }
}

/** 为 LoadingCache 的常用方法增加取消支持的包装实现。 */
class CancelableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, V>
) : LoadingCache<K, V> by delegate {
    override fun get(key: K, loader: Callable<out V>): V {
        return cancelable { delegate.get(key, loader) }
    }

    override fun getIfPresent(key: Any): V? {
        return cancelable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: MutableIterable<Any>): ImmutableMap<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }

    override fun get(key: K): V {
        return cancelable { delegate.get(key) }
    }

    override fun getUnchecked(key: K): V {
        return cancelable { delegate.getUnchecked(key) }
    }

    override fun getAll(keys: MutableIterable<K>): ImmutableMap<K, V> {
        return cancelable { delegate.getAll(keys) }
    }
}

@Suppress("UNCHECKED_CAST")
/** 将 null 值以占位符方式缓存，并提供便捷的 null 读取方法。 */
class ValueNullableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, Any>
) : LoadingCache<K, Any> by delegate {
    /**
     * 从缓存中获取指定键对应的值；若底层占位表示 null，则返回 null。
     */
    fun getOrNull(key: K): V? {
        return get(key) as? V
    }

    /**
     * 非检查方式从缓存中获取指定键对应的值；若底层占位表示 null，则返回 null。
     */
    fun getUncheckedOrNull(key: K): V? {
        return getUnchecked(key) as? V
    }
}

/** 基于 ModificationTracker 自动检测并失效条目的缓存包装。 */
class TrackingCache<K : Any, V : Any, C : Cache<K, V>>(
    private val delegate: C,
    private val modificationTrackerProvider: (V) -> ModificationTracker?
) : Cache<K, V> by delegate {
    private val modificationCounts = hashMapOf<K, Long>()

    override fun get(key: K, loader: Callable<out V>): V {
        val result = delegate.get(key, loader)
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        modificationCounts.put(key, newModificationCount)
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        val newResult = delegate.get(key, loader)
        return newResult
    }

    override fun getIfPresent(key: Any): V? {
        val result = delegate.getIfPresent(key) ?: return null
        val newModificationCount = modificationTrackerProvider(result)?.modificationCount
        if (newModificationCount == null) return result
        val oldModificationCount = modificationCounts.get(key)
        if (oldModificationCount == newModificationCount) return result
        if (oldModificationCount == null) return result
        delegate.invalidate(key)
        return null
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

/** 维护多实例缓存的外层索引容器。 */
class NestedCache<RK : Any, K : Any, V : Any, C : Cache<K, V>>(
    private val cacheProvider: () -> C
) {
    private val cacheMap = ConcurrentHashMap<RK, C>()

    /**
     * 获取或创建与外层键 [key] 对应的内层缓存实例（惰性初始化并复用）。
     */
    operator fun get(key: RK): C {
        return cacheMap.computeIfAbsent(key) { cacheProvider() }
    }
}
