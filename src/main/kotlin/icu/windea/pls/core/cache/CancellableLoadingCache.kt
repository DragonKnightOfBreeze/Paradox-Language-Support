package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.LoadingCache
import icu.windea.pls.core.cancellable
import icu.windea.pls.core.checkCancellation
import java.util.function.Function

/**
 * 可取消的载入缓存。使用 [cancellable] 包装取值方法。
 *
 * @see cancellable
 * @see checkCancellation
 */
class CancellableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, V>
) : LoadingCache<K, V> by delegate {
    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        return cancellable { delegate.get(key, mappingFunction) }
    }

    override fun getIfPresent(key: K): V? {
        return cancellable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: Iterable<K>): Map<K, V> {
        return cancellable { delegate.getAllPresent(keys) }
    }

    override fun get(key: K): V {
        return cancellable { delegate.get(key) }
    }

    override fun getAll(keys: Iterable<K>): Map<K, V> {
        return cancellable { delegate.getAll(keys) }
    }
}
