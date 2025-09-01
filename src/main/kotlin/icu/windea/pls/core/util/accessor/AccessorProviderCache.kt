package icu.windea.pls.core.util.accessor

import com.jetbrains.rd.util.ConcurrentHashMap
import icu.windea.pls.core.cast
import kotlin.reflect.KClass

/**
 * 访问器提供器缓存。
 *
 * 按目标类型缓存并复用 [AccessorProvider] 实例，避免重复创建。
 */
object AccessorProviderCache {
    private val cache = ConcurrentHashMap<KClass<*>, AccessorProvider<*>>()

    /** 获取（或创建并缓存）指定目标类型的 [AccessorProvider]。 */
    fun <T : Any> get(targetClass: KClass<T>): AccessorProvider<T> {
        return cache.getOrPut(targetClass) { AccessorProviderImpl(targetClass) }.cast()
    }
}
