package icu.windea.pls.images

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.nio.file.Path

/**
 * 图片渲染会话。
 *
 * 用于记录一组需要等待完成的渲染任务；任务会使用会话的 [scope] 作为父作用域。
 */
class ImageRenderSession(
    val scope: CoroutineScope
) {
    private val jobs = mutableListOf<Deferred<Path?>>()

    fun record(request: ImageRenderRequest): Deferred<Path?> {
        val job = scope.async(Dispatchers.IO) {
            ImageService.getInstance().renderImage(request)
        }
        synchronized(jobs) { jobs += job }
        return job
    }

    suspend fun awaitAll(): List<Path?> {
        val currentJobs = synchronized(jobs) { jobs.toList() }
        return currentJobs.awaitAll()
    }
}
