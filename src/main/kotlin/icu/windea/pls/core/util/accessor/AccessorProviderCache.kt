package icu.windea.pls.core.util.accessor

import com.jetbrains.rd.util.ConcurrentHashMap
import icu.windea.pls.core.cast
import kotlin.reflect.KClass

/**
 * [AccessorProvider] 缓存。
 *
 * 按目标 [KClass] 维度缓存/复用 [AccessorProvider] 实例，避免重复构建与反射扫描。
 */
object AccessorProviderCache {
    private val cache = ConcurrentHashMap<KClass<*>, AccessorProvider<*>>()

    /** 获取目标类型 [targetClass] 对应的 [AccessorProvider]，若不存在则创建并缓存。*/
    fun <T : Any> get(targetClass: KClass<T>): AccessorProvider<T> {
        return cache.getOrPut(targetClass) { AccessorProviderImpl(targetClass) }.cast()
    }
}
