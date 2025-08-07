package icu.windea.pls.core.util.accessor

import com.jetbrains.rd.util.*
import icu.windea.pls.core.*
import kotlin.reflect.*

object AccessorProviderCache {
    private val cache = ConcurrentHashMap<KClass<*>, AccessorProvider<*>>()

    fun <T : Any> get(targetClass: KClass<T>): AccessorProvider<T> {
        return cache.getOrPut(targetClass) { AccessorProviderImpl(targetClass) }.cast()
    }
}
