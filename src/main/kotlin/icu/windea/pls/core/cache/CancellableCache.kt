package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.Cache
import icu.windea.pls.core.cancellable
import icu.windea.pls.core.checkCancellation
import java.util.function.Function

/**
 * 可取消的缓存。使用 [cancellable] 包装取值方法。
 *
 * @see cancellable
 * @see checkCancellation
 */
class CancellableCache<K : Any, V : Any>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        return cancellable { delegate.get(key, mappingFunction) }
    }

    override fun getIfPresent(key: K): V? {
        return cancellable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: Iterable<K>): Map<K, V> {
        return cancellable { delegate.getAllPresent(keys) }
    }
}
