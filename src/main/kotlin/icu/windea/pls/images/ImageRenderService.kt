package icu.windea.pls.images

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.fileSizeSafe
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.withContext
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.isRegularFile
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 图片渲染服务。
 *
 * 负责图片缓存检查、同步/后台渲染、渲染会话，以及图片磁盘缓存的定期清理。
 */
@Service
class ImageRenderService : Disposable {
    companion object {
        private val logger = logger<ImageRenderService>()

        @JvmStatic
        fun getInstance(): ImageRenderService = service()
    }

    private val renderFutures = ConcurrentHashMap<ImageRenderRequest, CompletableFuture<Path?>>()
    private val renderSession = ThreadLocal<ImageRenderSession?>()
    private val cacheCleaner = ImageCacheCleaner()

    @Volatile
    private var cleanupFuture: ScheduledFuture<*>? = null

    fun newRenderSession(scope: CoroutineScope): ImageRenderSession = ImageRenderSession(scope)

    fun currentRenderSession(): ImageRenderSession? = renderSession.get()

    fun <T> withRenderSession(session: ImageRenderSession, action: () -> T): T {
        val oldSession = renderSession.get()
        renderSession.set(session)
        try {
            return action()
        } finally {
            if (oldSession == null) renderSession.remove() else renderSession.set(oldSession)
        }
    }

    /**
     * 如果存在对应的本地缓存，则直接返回预期的目标路径。
     */
    fun getCachedImagePath(request: ImageRenderRequest): Path? {
        val targetPath = request.targetPath
        if (!targetPath.isRegularFile() || targetPath.fileSizeSafe() == 0L) return null
        return targetPath
    }

    /**
     * 尝试在当前渲染会话中记录渲染请求，并返回预期的目标路径。
     *
     * 如果当前没有渲染会话，则不会自动开始渲染；调用方应使用 [requestImageRender]。
     */
    fun getRecordedImagePath(request: ImageRenderRequest): Path? {
        val currentSession = currentRenderSession() ?: return null
        @Suppress("DeferredResultUnused") currentSession.launch(request)
        return request.targetPath
    }

    /**
     * 同步请求渲染图片，并在后台进度中等待完成。相同请求会被合并。
     *
     * 注意：不能在 EDT 中调用；如果在 EDT 中调用，只会返回已有缓存或 `null`。
     */
    fun requestImageRender(request: ImageRenderRequest, project: Project): Path? {
        if (project.isDisposed) return null
        getCachedImagePath(request)?.let { return it }

        val application = ApplicationManager.getApplication()
        if (application != null && application.isDispatchThread) {
            logger.warn("Skip image rendering in EDT: ${request.sourcePath}")
            return null
        }

        return requestImageRenderAsync(request, project).get()
    }

    /**
     * 在后台请求渲染图片。相同请求会被合并。
     */
    fun requestImageRenderAsync(request: ImageRenderRequest, project: Project): CompletableFuture<Path?> {
        if (project.isDisposed) return CompletableFuture.completedFuture(null)
        getCachedImagePath(request)?.let { return CompletableFuture.completedFuture(it) }

        renderFutures.get(request)?.let { return it }

        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        val job = coroutineScope.async {
            withBackgroundProgress(project, ChronicleBundle.message("image.render.progress.title"), cancellable = true) {
                withContext(Dispatchers.IO) { renderImage(request) }
            }
        }
        // Note that `runBlockingCancellable` relies on the job/indicator already present on the current thread,
        // and using it directly throws an exception at call sites without a progress context,
        // so we use `CompletableFuture` instead of `Deferred` here.
        return job.asCompletableFuture() // use `Deferred.asCompletableFuture()` instead of manually construction
    }

    /**
     * 同步渲染图片到目标缓存路径；如果目标已存在则直接返回。
     */
    fun renderImage(request: ImageRenderRequest): Path? {
        getCachedImagePath(request)?.let { return it }
        val sourcePath = request.sourcePath
        val targetPath = request.targetPath
        if (sourcePath == targetPath) return targetPath
        val targetParent = targetPath.parent
        if (targetParent != null) Files.createDirectories(targetParent)

        try {
            ProgressManager.checkCanceled()
            val sourceImage = ImageIO.read(sourcePath.toFile()) ?: return null
            ProgressManager.checkCanceled()
            val slicedImage = request.frameInfo?.let { sliceImage(sourceImage, it) } ?: sourceImage
            val finalImage = scaleImage(slicedImage, request.maxWidth, request.maxHeight)
            ProgressManager.checkCanceled()
            val written = ImageIO.write(finalImage, request.targetFormat, targetPath.toFile())
            if (!written) throw IllegalStateException("No image writer found for format: ${request.targetFormat}")
            return targetPath
        } catch (e: Throwable) {
            runCatching { Files.deleteIfExists(targetPath) }
            throw e
        }
    }

    fun scaleImage(image: BufferedImage, maxWidth: Int?, maxHeight: Int?): BufferedImage {
        if (maxWidth == null && maxHeight == null) return image
        val widthScale = maxWidth?.let { it.toDouble() / image.width } ?: 1.0
        val heightScale = maxHeight?.let { it.toDouble() / image.height } ?: 1.0
        val scale = min(1.0, min(widthScale, heightScale))
        if (scale >= 1.0) return image
        val type = BufferedImage.TYPE_INT_ARGB
        val width = (image.width * scale).roundToInt().coerceAtLeast(1)
        val height = (image.height * scale).roundToInt().coerceAtLeast(1)
        @Suppress("UndesirableClassUsage") // NOTE 3.0.5 （目前认为）这里应当直接构造 `BufferedImage`，不进行缩放
        val result = BufferedImage(width, height, type)
        // val result = ImageUtil.createImage(width, height, type)
        val graphics = result.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.drawImage(image, 0, 0, width, height, null)
        } finally {
            graphics.dispose()
        }
        return result
    }

    fun sliceImage(image: BufferedImage, frameInfo: ImageFrameInfo): BufferedImage? {
        if (!frameInfo.canApply()) return null
        val width = image.width
        val height = image.height
        val finalFrames = if (frameInfo.frames > 0) frameInfo.frames else width / height
        val finalFrame = if (frameInfo.frame > finalFrames) finalFrames else frameInfo.frame
        val frameWidth = width / finalFrames
        val startX = (finalFrame - 1) * frameWidth
        return image.getSubimage(startX, 0, frameWidth, height)
    }

    /**
     * 启动图片磁盘缓存的定期清理。重复调用不会重复调度。
     */
    @Synchronized
    fun startCacheCleanup() {
        if (cleanupFuture != null) return
        val executor = AppExecutorUtil.getAppScheduledExecutorService()
        executor.execute { cleanupImageCache() }
        cleanupFuture = executor.scheduleWithFixedDelay({ cleanupImageCache() }, 1, 1, TimeUnit.HOURS)
    }

    private fun cleanupImageCache() {
        try {
            cacheCleaner.cleanup()
        } catch (e: Exception) {
            logger.warn("Failed to clean up image cache.", e)
        }
    }

    override fun dispose() {
        cleanupFuture?.cancel(false)
        cleanupFuture = null
        renderFutures.values.forEach { it.cancel(true) }
        renderFutures.clear()
        renderSession.remove()
    }
}
