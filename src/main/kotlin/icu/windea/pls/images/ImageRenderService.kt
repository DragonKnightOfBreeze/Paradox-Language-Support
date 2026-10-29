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
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.core.util.io.readImage
import icu.windea.pls.core.util.io.writeImage
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.withContext
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days

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
        val future = job.asCompletableFuture().thenApply<Path?> { it } // use `Deferred.asCompletableFuture()` instead of manually construction
        val existing = renderFutures.putIfAbsent(request, future)
        if (existing != null) {
            future.cancel(true)
            return existing
        }
        future.whenComplete { _, _ -> renderFutures.remove(request, future) }
        return future
    }

    /**
     * 同步渲染图片到目标缓存路径；如果目标已存在则直接返回。
     */
    fun renderImage(request: ImageRenderRequest): Path {
        getCachedImagePath(request)?.let { return it }
        val sourcePath = request.sourcePath
        val targetPath = request.targetPath
        if (sourcePath == targetPath) return targetPath
        val targetParent = targetPath.parent
        if (targetParent != null) Files.createDirectories(targetParent)

        try {
            ProgressManager.checkCanceled()
            val sourceImage = readImage(sourcePath) ?: throw IllegalStateException("No image reader found for: $sourcePath")
            ProgressManager.checkCanceled()
            val slicedImage = request.frameInfo?.let { sliceImage(sourceImage, it) } ?: sourceImage
            val finalImage = scaleImage(slicedImage, request.maxWidth, request.maxHeight)
            ProgressManager.checkCanceled()
            val written = writeImage(finalImage, request.targetFormat, targetPath)
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
            val settings = ChronicleImageSettings.getInstance().state
            val cleaner = ImageCacheCleaner(
                ttl = settings.cacheTtlDays.coerceAtLeast(0).days,
                maxSize = settings.cacheMaxSizeMb.coerceAtLeast(0).toLong() * 1024 * 1024,
                maxFiles = settings.cacheMaxFiles.coerceAtLeast(0),
            )
            cleaner.cleanup(settings.cacheCleanupPolicy)
        } catch (e: Exception) {
            logger.warn("Failed to clean up image cache.", e)
        }
    }

    /**
     * 返回图片渲染失败时使用的占位图路径。占位图会在首次使用时生成。
     */
    @Synchronized
    @Suppress("UseJBColor", "UndesirableClassUsage")
    fun getPlaceholderImagePath(): Path {
        val placeholderPath = ChronicleDataPathService.getInstance().imagesPath.resolve("_placeholder.png")
        if (placeholderPath.isRegularFile() && placeholderPath.fileSizeSafe() > 0L) return placeholderPath
        placeholderPath.parent?.createDirectories()
        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.composite = AlphaComposite.Src
            graphics.color = Color(0x80, 0x80, 0x80, 0x80)
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.color = Color(0x40, 0x40, 0x40, 0xFF)
            graphics.drawLine(0, 0, image.width - 1, image.height - 1)
            graphics.drawLine(image.width - 1, 0, 0, image.height - 1)
        } finally {
            graphics.dispose()
        }
        writeImage(image, "png", placeholderPath)
        return placeholderPath
    }

    /**
     * 根据 [ImageRenderFailurePolicy] 处理渲染会话中失败的图片。
     */
    fun applyFailurePolicy(builder: DocumentationBuilder, session: ImageRenderSession) {
        val failedRequests = session.failedRequests()
        if (failedRequests.isEmpty()) return
        val policy = ChronicleImageSettings.getInstance().state.renderFailurePolicy
        for (request in failedRequests) {
            when (policy) {
                ImageRenderFailurePolicy.AcceptBroken -> Unit
                ImageRenderFailurePolicy.RemoveImage -> builder.removeImage(request.targetPath.absolutePathString())
                ImageRenderFailurePolicy.Placeholder -> {
                    val placeholderPath = runCatching { getPlaceholderImagePath() }.getOrNull() ?: continue
                    builder.replaceImage(request.targetPath.absolutePathString(), placeholderPath.absolutePathString())
                }
            }
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
