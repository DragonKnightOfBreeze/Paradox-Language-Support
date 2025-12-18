package icu.windea.pls.core.cache

import com.github.benmanes.caffeine.cache.LoadingCache
import icu.windea.pls.core.cancelable
import java.util.function.Function

/**
 * 可取消的载入缓存。使用 [icu.windea.pls.core.cancelable] 包装取值方法。
 *
 * @see com.intellij.openapi.progress.ProcessCanceledException
 */
class CancelableLoadingCache<K : Any, V : Any>(
    private val delegate: LoadingCache<K, V>
) : LoadingCache<K, V> by delegate {
    override fun get(key: K, mappingFunction: Function<in K, out V>): V {
        return cancelable { delegate.get(key, mappingFunction) }
    }

    override fun getIfPresent(key: K): V? {
        return cancelable { delegate.getIfPresent(key) }
    }

    override fun getAllPresent(keys: Iterable<K>): Map<K, V> {
        return cancelable { delegate.getAllPresent(keys) }
    }

    override fun get(key: K): V {
        return cancelable { delegate.get(key) }
    }

    override fun getAll(keys: Iterable<K>): Map<K, V> {
        return cancelable { delegate.getAll(keys) }
    }
}
