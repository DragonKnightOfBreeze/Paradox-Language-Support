package icu.windea.pls.core.accessor

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import icu.windea.pls.core.cast
import kotlinx.coroutines.CancellationException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object AccessorContext {
    private val logger = thisLogger()
    private val reported = ConcurrentHashMap.newKeySet<String>()

    private val cache = ConcurrentHashMap<KClass<*>, AccessorProvider<*>>()

    /**
     * 获取目标类型 [targetClass] 对应的 [AccessorProvider]，若不存在则创建并缓存。
     *
     * 按目标 [KClass] 维度缓存和复用 [AccessorProvider] 实例，避免重复构建和反射扫描。
     */
    @JvmStatic
    fun <T : Any> get(targetClass: KClass<T>): AccessorProvider<T> {
        // 3.0.1 use `computeIfAbsent` here to ensure strict thread safe
        return cache.computeIfAbsent(targetClass) { AccessorProviderImpl(targetClass) }.cast()
    }

    @JvmStatic
    fun reportError(name: String, error: Throwable) {
        val key = name
        if (!reported.add(key)) return
        logger.warn("ERROR while execute $name (suppressed now)", error)
    }

    @PublishedApi
    internal inline fun <T> runInAccessorDelegate(action: () -> T): T {
        try {
            return action()
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }

    @PublishedApi
    internal inline fun <T> runInAccessorProvider(action: () -> T): T {
        try {
            return action()
        } catch (e: Exception) {
            if (e is InvocationTargetException) throw e.targetException ?: e // 3.0.1 fix: not `e.cause` here
            if (e is ProcessCanceledException || e is CancellationException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }
}
