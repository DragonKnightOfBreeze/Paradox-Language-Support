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
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.core.runCatchingCancellable
import icu.windea.pls.core.ui.toIcon
import icu.windea.pls.core.util.ImageFrameInfo
import icu.windea.pls.core.util.cache.CacheBuilder
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
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

    /**
     * 内存级图片/图标缓存键。
     *
     * 键中包含源文件的大小与修改时间，因此源文件变更后会自动失效；
     * 目标路径只用于区分不同的渲染产物，不参与磁盘状态判断。
     */
    private data class ImageCacheKey(
        val sourcePath: Path,
        val sourceSize: Long,
        val sourceModifiedAt: Long,
        val targetPath: Path,
        val frameInfo: ImageFrameInfo?,
    )

    private val renderFutures = ConcurrentHashMap<ImageDescriptor, CompletableFuture<Path?>>()
    private val imageFutures = ConcurrentHashMap<ImageCacheKey, CompletableFuture<Image?>>()
    private val renderSession = ThreadLocal<ImageRenderSession?>()

    // 有界内存缓存：以解码后的图片/图标实例复用为主，因此不暴露任何配置项，直接写死合理常量
    private val imageCache = CacheBuilder("expireAfterAccess=10m")
        .maximumWeight(32L * 1024 * 1024)
        .weigher<ImageCacheKey, Image> { _, image ->
            val width = image.getWidth(null).coerceAtLeast(1).toLong()
            val height = image.getHeight(null).coerceAtLeast(1).toLong()
            (width * height * 4L).coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
        }
        .build<ImageCacheKey, Image>()

    private val iconCache = CacheBuilder("maximumSize=2048,expireAfterAccess=10m")
        .build<ImageCacheKey, Icon>()

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
     * 同步请求渲染图片，并在后台进度中等待完成。相同请求会被合并。
     *
     * 注意：不能在 EDT 中调用；如果在 EDT 中调用，只会返回已有缓存或 `null`。
     */
    fun requestImageRender(descriptor: ImageDescriptor, project: Project): Path? {
        if (project.isDisposed) return null
        getCachedImagePath(descriptor)?.let { return it }

        val application = ApplicationManager.getApplication()
        if (application != null && application.isDispatchThread) {
            logger.warn("Skip image rendering in EDT: ${descriptor.sourcePath}")
            return null
        }

        return requestImageRenderAsync(descriptor, project).get()
    }

    /**
     * 在后台请求渲染图片。相同请求会被合并。
     */
    fun requestImageRenderAsync(descriptor: ImageDescriptor, project: Project): CompletableFuture<Path?> {
        if (project.isDisposed) return CompletableFuture.completedFuture(null)
        getCachedImagePath(descriptor)?.let { return CompletableFuture.completedFuture(it) }

        renderFutures.get(descriptor)?.let { return it }

        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        val job = coroutineScope.async {
            withBackgroundProgress(project, ChronicleBundle.message("image.render.progress.title"), cancellable = true) {
                withContext(Dispatchers.IO) { renderImage(descriptor) }
            }
        }
        // Note that `runBlockingCancellable` relies on the job/indicator already present on the current thread,
        // and using it directly throws an exception at call sites without a progress context,
        // so we use `CompletableFuture` instead of `Deferred` here.
        val future = job.asCompletableFuture().thenApply { it } // use `Deferred.asCompletableFuture()` instead of manually construction
        val existing = renderFutures.putIfAbsent(descriptor, future)
        if (existing != null) {
            future.cancel(true)
            return existing
        }
        future.whenComplete { _, _ -> renderFutures.remove(descriptor, future) }
        return future
    }

    /**
     * 同步渲染图片到目标缓存路径；如果目标已存在则直接返回。
     *
     * 备注：
     * - 渲染结果会先写入同目录的 `.part` 临时文件，校验通过后再原子落位到目标路径。
     * - 异常时不会留下残缺的目标文件，且无论成功与否都会清理临时文件。
     */
    fun renderImage(descriptor: ImageDescriptor): Path {
        getCachedImagePath(descriptor)?.let { return it }
        val sourcePath = descriptor.sourcePath
        val targetPath = descriptor.targetPath
        if (sourcePath == targetPath) return targetPath
        val targetParent = targetPath.parent
        if (targetParent != null) targetParent.createDirectories()

        val partPath = targetPath.resolveSibling("${targetPath.fileName}.${UUID.randomUUID()}.part")
        var moved = false
        try {
            ProgressManager.checkCanceled()
            val sourceImage = ImageIoService.readImage(sourcePath)
            ProgressManager.checkCanceled()
            val finalImage = descriptor.frameInfo?.let { ImageIoService.sliceImage(sourceImage, it) } ?: sourceImage
            ProgressManager.checkCanceled()
            val written = ImageIoService.writeImage(finalImage, descriptor.targetFormat, partPath)
            if (!written) throw IllegalStateException("No image writer found for format: ${descriptor.targetFormat}")
            if (!ImageIoService.isValidImageFile(partPath, descriptor.targetFormat)) {
                throw IllegalStateException("Invalid image file after rendering: $partPath")
            }
            moveAtomically(partPath, targetPath)
            moved = true
            return targetPath
        } catch (e: Throwable) {
            if (!moved) runCatching { Files.deleteIfExists(targetPath) }
            throw e
        } finally {
            runCatching { Files.deleteIfExists(partPath) }
        }
    }

    /**
     * 将临时文件原子地移动到目标路径。
     *
     * 优先尝试 `ATOMIC_MOVE`；当文件系统不支持时回退为仅使用 `REPLACE_EXISTING`。
     */
    private fun moveAtomically(partPath: Path, targetPath: Path) {
        try {
            Files.move(partPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(partPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    /**
     * 在后台物化图片，并返回可直接用于展示的 [Image]。
     *
     * 该方法会读取图片的原始元数据、检查高度限制、按需渲染并解码目标图片。
     * 调用方应确保当前不在 EDT/readAction 中，并按需添加超时。
     *
     * 备注：
     * - 对于 `Delegated` 策略，如果缓存文件解码失败（但轻量校验通过），会删除损坏的缓存并重试一次。
     */
    fun createImage(descriptor: ImageDescriptor, heightLimit: Int? = null): Image? {
        val key = createImageCacheKey(descriptor) ?: return null
        return createImage(key, descriptor, heightLimit)
    }

    /**
     * 在后台物化图片，并返回可直接用于展示的 [Icon]。
     *
     * 该方法会读取图片的原始元数据、检查高度限制、按需渲染并解码目标图片。
     * 调用方应确保当前不在 EDT/readAction 中，并按需添加超时。
     */
    fun createImageIcon(descriptor: ImageDescriptor, heightLimit: Int? = null): Icon? {
        val key = createImageCacheKey(descriptor) ?: return null
        val image = createImage(key, descriptor, heightLimit) ?: return null
        // 复用同一个 `JBImageIcon` 实例，避免多张卡片重复创建图标对象
        return iconCache.get(key) { image.toIcon() }
    }

    /**
     * 构造内存缓存键。
     *
     * 如果源文件不存在、不是常规文件或为空，则快速返回 `null`（调用方随后直接返回 `null`）。
     */
    private fun createImageCacheKey(descriptor: ImageDescriptor): ImageCacheKey? {
        val sourcePath = descriptor.sourcePath.toAbsolutePath().normalize()
        if (!sourcePath.isRegularFile()) return null
        val sourceSize = runCatchingCancellable { Files.size(sourcePath) }.getOrNull() ?: return null
        if (sourceSize <= 0L) return null
        val sourceModifiedAt = runCatchingCancellable { Files.getLastModifiedTime(sourcePath).toMillis() }.getOrNull() ?: return null
        val targetPath = descriptor.targetPath.toAbsolutePath().normalize()
        return ImageCacheKey(sourcePath, sourceSize, sourceModifiedAt, targetPath, descriptor.frameInfo)
    }

    private fun createImage(key: ImageCacheKey, descriptor: ImageDescriptor, heightLimit: Int?): Image? {
        // 内存缓存命中：完全跳过元数据读取与磁盘读取
        imageCache.getIfPresent(key)?.let { cachedImage -> return cachedImage.applyHeightLimit(heightLimit) }

        // 元数据读取剪枝：仅在需要检查高度限制时才读取元数据
        if (heightLimit != null) {
            val metadata = ImageMetadataService.getInstance().getMetadataOrNull(descriptor.sourcePath) ?: return null
            if (metadata.height > heightLimit) return null
        }

        val image = awaitOrDecodeImage(key, descriptor)
        // 解码结果的高度对同一缓存键而言是恒定的，这里同时覆盖“缓存高度未知”的兜底场景
        return image.applyHeightLimit(heightLimit)
    }

    /**
     * 合并同一 [ImageCacheKey] 的并发解码工作（Single-flight）。
     *
     * 如果等待方收到前置请求抛出的取消异常，但当前线程自身并未被取消，
     * 则独立降级执行一次，避免单个请求的取消连累其他正常并发请求。
     */
    private fun awaitOrDecodeImage(key: ImageCacheKey, descriptor: ImageDescriptor): Image? {
        // 先尝试复用在途请求
        imageFutures[key]?.let { existing ->
            when (val result = awaitImageFuture(existing)) {
                is ImageAwaitResult.Value -> return imageCache.getIfPresent(key) ?: result.image
                // 当前线程未被取消：移除已取消的前置请求，随后独立降级执行一次
                ImageAwaitResult.Retry -> imageFutures.remove(key, existing)
            }
        }

        // 成为当前缓存键的渲染发起方
        val future = CompletableFuture<Image?>()
        val leader = imageFutures.putIfAbsent(key, future)
        if (leader != null) {
            // 期间已有其他线程成为发起方：再等待一次，不再继续重试
            return when (val result = awaitImageFuture(leader)) {
                is ImageAwaitResult.Value -> imageCache.getIfPresent(key) ?: result.image
                ImageAwaitResult.Retry -> {
                    imageFutures.remove(key, leader)
                    null
                }
            }
        }

        try {
            // 在真正解码前再次检查内存缓存，避免在竞争窗口内重复解码
            imageCache.getIfPresent(key)?.let { image ->
                future.complete(image)
                return image
            }
            val image = decodeImage(descriptor, key)
            future.complete(image)
            return image
        } catch (e: Throwable) {
            future.completeExceptionally(e)
            throw e
        } finally {
            imageFutures.remove(key, future)
        }
    }

    private fun awaitImageFuture(future: CompletableFuture<Image?>): ImageAwaitResult {
        return try {
            ImageAwaitResult.Value(future.get())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (e: ExecutionException) {
            val cause = e.cause ?: e
            if (cause.isCancellation()) {
                if (isCurrentThreadCancelled()) throw cause
                ImageAwaitResult.Retry
            } else {
                throw cause
            }
        } catch (e: CancellationException) {
            if (isCurrentThreadCancelled()) throw e
            ImageAwaitResult.Retry
        }
    }

    private fun decodeImage(descriptor: ImageDescriptor, key: ImageCacheKey): Image {
        val targetPath = renderImage(descriptor)
        val image = try {
            ImageIoService.readImage(targetPath)
        } catch (e: Throwable) {
            checkCancellation(e)
            if (descriptor.strategy != ImageStrategy.Delegated) throw e
            runCatching { Files.deleteIfExists(targetPath) }
            val retriedPath = renderImage(descriptor)
            ImageIoService.readImage(retriedPath)
        }
        imageCache.put(key, image)
        return image
    }

    /**
     * 检查当前线程自身是否确实被取消（而不是收到其他请求传播的取消异常）。
     */
    private fun isCurrentThreadCancelled(): Boolean {
        // 这里只需要判断当前线程自身是否已被取消，不能直接调用 `ProgressManager.checkCanceled()`，
        // 否则会把前置请求传播来的取消异常误判为当前线程的取消。
        return ProgressManager.getInstance().progressIndicator?.isCanceled == true
    }

    private fun Throwable.isCancellation(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is CancellationException) return true
            current = current.cause
        }
        return false
    }

    private fun Image?.applyHeightLimit(heightLimit: Int?): Image? {
        val image = this ?: return null
        if (heightLimit == null) return image
        val height = image.getHeight(null)
        if (height >= 0 && height > heightLimit) return null
        return image
    }

    private sealed interface ImageAwaitResult {
        data class Value(val image: Image?) : ImageAwaitResult
        data object Retry : ImageAwaitResult
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
     * 如果存在对应的本地缓存，则直接返回预期的目标路径。
     *
     * 备注：
     * - 对 `Delegated` 策略且目标格式为 `png` 的缓存做头尾魔数校验，发现损坏文件时安全删除并返回 `null`。
     * - 对 `Delegated` 策略命中缓存时，若缓存的修改时间距今超过 1 天，则更新其修改时间以实现节流 Touch。
     * - 对 `Direct` 策略不会修改任何文件的修改时间。
     */
    fun getCachedImagePath(descriptor: ImageDescriptor): Path? {
        val targetPath = descriptor.targetPath
        if (!targetPath.isRegularFile() || targetPath.fileSizeSafe() == 0L) return null
        if (descriptor.strategy == ImageStrategy.Delegated) {
            if (!ImageIoService.isValidImageFile(targetPath, descriptor.targetFormat)) {
                runCatching { Files.deleteIfExists(targetPath) }
                return null
            }
            touchCacheFile(targetPath)
        }
        return targetPath
    }

    /**
     * 节流更新缓存的修改时间：仅当距上次修改超过 1 天时才刷新。
     */
    private fun touchCacheFile(path: Path) {
        runCatching {
            val now = System.currentTimeMillis()
            val lastModified = Files.getLastModifiedTime(path).toMillis()
            if (now - lastModified > 1.days.inWholeMilliseconds) {
                Files.setLastModifiedTime(path, FileTime.fromMillis(now))
            }
        }
    }

    /**
     * 判断源图片是否可以解析（即存在且非空）。
     */
    fun canResolve(descriptor: ImageDescriptor): Boolean {
        val sourcePath = descriptor.sourcePath
        if (!(sourcePath.exists() && sourcePath.fileSizeSafe() > 0)) return false
        return true
    }

    /**
     * 解析得到图片的最终地址。
     *
     * 说明：
     * - 如果存在当前渲染会话，则首先在其中记录渲染请求。
     * - 如果不需要渲染，则直接返回源路径。
     * - 如果存在目标路径对应的本地缓存，则直接返回目标路径。
     * - 如果存在当前渲染会话，则直接返回目标路径。这适用于渲染快速文档等场景。
     * - 否则，尝试同步获取或渲染目标图片。
     * - 同步渲染失败时，按照设置中的失败策略返回目标路径、`null` 或占位图路径。
     */
    fun resolveUrl(descriptor: ImageDescriptor, project: Project): String? {
        val session = currentRenderSession()

        // 3.0.5 it's necessary to record render request first, or image policies may be not applied correctly
        @Suppress("DeferredResultUnused") session?.launch(descriptor)

        try {
            if (!descriptor.renderRequired) return descriptor.sourcePath.absolutePathString()
            val cachedPath = getCachedImagePath(descriptor)
            if (cachedPath != null) return cachedPath.absolutePathString()
            if (session != null) return descriptor.targetPath.absolutePathString()
            return requestImageRender(descriptor, project)?.absolutePathString()
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Render image failed. (source path: ${descriptor.sourcePath})", e)
            return when (ChronicleImageSettings.getInstance().state.renderFailurePolicy) {
                ImageRenderFailurePolicy.AcceptBroken -> descriptor.targetPath.absolutePathString()
                ImageRenderFailurePolicy.RemoveImage -> null
                ImageRenderFailurePolicy.Placeholder -> {
                    runCatching { getPlaceholderImagePath().absolutePathString() }.getOrNull()
                }
            }
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
        ImageIoService.writeImage(image, "png", placeholderPath)
        return placeholderPath
    }

    /**
     * 对快速文档中的文本图标按文本高度进行缩放。
     *
     * 只处理高度不超过 [textIconSizeLimit] 的图片；较大的图片保留原始尺寸。
     */
    fun applyImageScaling(builder: DocumentationBuilder, session: ImageRenderSession, textHeight: Int, textIconSizeLimit: Int) {
        if (textHeight <= 0 || textIconSizeLimit <= 0) return
        for (request in session.requests()) {
            val targetPath = request.targetPath
            if (!targetPath.isRegularFile()) continue
            // 优先读取内存缓存中的图像宽高，未命中时再回退到元数据读取
            var width = 0
            var height = 0
            val cachedImage = createImageCacheKey(request)?.let { imageCache.getIfPresent(it) }
            if (cachedImage != null) {
                val cachedWidth = cachedImage.getWidth(null)
                val cachedHeight = cachedImage.getHeight(null)
                if (cachedWidth > 0 && cachedHeight > 0) {
                    width = cachedWidth
                    height = cachedHeight
                }
            }
            if (height <= 0) {
                val metadata = ImageMetadataService.getInstance().getMetadataOrNull(targetPath) ?: continue
                width = metadata.width
                height = metadata.height
            }
            if (height > textIconSizeLimit) continue
            val scaledWidth = (width * (textHeight.toDouble() / height)).roundToInt().coerceAtLeast(1)
            builder.resizeImage(targetPath.absolutePathString(), scaledWidth, textHeight)
        }
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

    /**
     * 清理内存级的图片/图标缓存，仅供测试使用。
     */
    @TestOnly
    @Suppress("unused")
    fun invalidateMemoryCache() {
        imageCache.invalidateAll()
        iconCache.invalidateAll()
    }

    override fun dispose() {
        cleanupFuture?.cancel(false)
        cleanupFuture = null
        renderFutures.values.forEach { it.cancel(true) }
        renderFutures.clear()
        imageFutures.values.forEach { it.cancel(true) }
        imageFutures.clear()
        imageCache.invalidateAll()
        iconCache.invalidateAll()
        renderSession.remove()
    }
}
