package icu.windea.pls.images

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.coroutineToIndicator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 图片渲染会话。
 *
 * 用于记录一组需要等待完成的渲染任务；任务会使用会话的 [scope] 作为父作用域。
 */
class ImageRenderSession(
    val scope: CoroutineScope,
) {
    // 3.0.5 统一使用 `ConcurrentLinkedQueue`，无锁且并发遍历绝对安全，以省去不必要的心智负担
    private val jobs = ConcurrentLinkedQueue<Deferred<Path?>>()
    private val failedRequests = ConcurrentLinkedQueue<ImageDescriptor>()
    private val descriptors = ConcurrentLinkedQueue<ImageDescriptor>()

    fun launch(descriptor: ImageDescriptor): Deferred<Path?> {
        descriptors += descriptor
        val job = scope.async {
            try {
                renderImage(descriptor)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failedRequests += descriptor
                logger.warn("Render image for documentation session failed. (source path: ${descriptor.sourcePath})", e)
                null
            }
        }
        jobs += job
        return job
    }

    private suspend fun renderImage(descriptor: ImageDescriptor): Path {
        return withContext(Dispatchers.IO) {
            coroutineToIndicator {
                ImageRenderService.getInstance().renderImage(descriptor)
            }
        }
    }

    suspend fun awaitAll(): List<Path?> {
        return jobs.toList().awaitAll()
    }

    fun requests(): List<ImageDescriptor> {
        return descriptors.toList()
    }

    fun failedRequests(): List<ImageDescriptor> {
        return failedRequests.toList()
    }

    companion object {
        private val logger = logger<ImageRenderSession>()
    }
}
