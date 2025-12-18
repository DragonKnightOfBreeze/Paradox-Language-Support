package icu.windea.pls.core.initializer

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.runCatchingCancelable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

/**
 * 初始化器。
 *
 * - 允许在启动阶段收集多个异步初始化任务并并行执行（见 [initialize]）；
 * - 提供 `await(...)` 族方法生成懒加载值，在所有初始化任务完成后读取；
 * - 适用于需要在 UI 可用后，后台准备资源（目录/文件/缓存等）的场景。
 */
class Initializer(
    private val completableDeferred: CompletableDeferred<Unit> = CompletableDeferred(),
    private val initializeActions: MutableList<suspend () -> Unit> = mutableListOf()
) {
    /** 并行执行已收集的初始化任务；执行完成后使 `await(...)` 的懒值可读取。*/
    suspend fun initialize() {
        if (initializeActions.isEmpty()) return
        val logger = thisLogger()
        coroutineScope {
            initializeActions.map { action ->
                async {
                    runCatchingCancelable { action() }.onFailure { logger.warn(it) }
                }
            }.awaitAll()
            initializeActions.clear() // 这里可以直接清空
            completableDeferred.complete(Unit)
        }
    }

    /** 返回直接包裹值 [value] 的懒对象（不注册初始化任务）。*/
    fun <T> await(value: T): Lazy<T> {
        return lazyOf(value)
    }

    /**
     * 注册一个针对值 [value] 的初始化任务 [initializeAction]，并返回懒对象：
     * 调用懒对象时，若初始化尚未完成则挂起等待。
     */
    fun <T> await(value: T, initializeAction: suspend (T) -> Unit): Lazy<T> {
        initializeActions.add { initializeAction(value) }
        return lazy {
            if (!completableDeferred.isCompleted) {
                runBlocking {
                    completableDeferred.await()
                }
            }
            value
        }
    }

    /** 与上类似，但在获取懒值时对 [value] 应用转换函数 [transform] 后返回。*/
    fun <T, R> await(value: T, initializeAction: suspend (T) -> Unit, transform: (T) -> R): Lazy<R> {
        initializeActions.add { initializeAction(value) }
        return lazy {
            if (!completableDeferred.isCompleted) {
                runBlocking {
                    completableDeferred.await()
                }
            }
            transform(value)
        }
    }
}
