package icu.windea.pls.images

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.coroutineToIndicator
import icu.windea.pls.core.collections.synced
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 图片渲染会话。
 *
 * 用于记录一组需要等待完成的渲染任务；任务会使用会话的 [scope] 作为父作用域。
 */
class ImageRenderSession(
    val scope: CoroutineScope
) {
    companion object {
        private val logger = logger<ImageRenderSession>()
    }

    private val jobs = mutableListOf<Deferred<Path?>>().synced()
    private val failedRequests = CopyOnWriteArrayList<ImageRenderRequest>()

    fun launch(request: ImageRenderRequest): Deferred<Path?> {
        val job = scope.async {
            try {
                withContext(Dispatchers.IO) {
                    coroutineToIndicator { _: ProgressIndicator ->
                        ImageRenderService.getInstance().renderImage(request)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failedRequests += request
                logger.warn("Render image for documentation session failed. (source path: ${request.sourcePath})", e)
                null
            }
        }
        jobs += job
        return job
    }

    suspend fun awaitAll(): List<Path?> {
        val currentJobs = jobs.toList()
        return currentJobs.awaitAll()
    }

    /**
     * 返回渲染失败的请求，用于应用 [ImageRenderFailurePolicy]。
     */
    fun failedRequests(): List<ImageRenderRequest> = failedRequests.toList()
}
