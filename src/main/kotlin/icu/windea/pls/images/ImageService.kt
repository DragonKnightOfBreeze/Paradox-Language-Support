package icu.windea.pls.images

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.spi.DdsImageReaderSpi
import icu.windea.pls.images.spi.TgaImageReaderSpi
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.model.constants.ChronicleConstants
import org.intellij.images.fileTypes.impl.ImageFileType
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.spi.IIORegistry
import kotlin.math.min
import kotlin.math.roundToInt

@Service
class ImageService : AppLifecycleListener, DynamicPluginListener {
    companion object {
        private val logger = logger<ImageService>()

        @JvmStatic
        fun getInstance(): ImageService = service()
    }

    private val renderFutures = ConcurrentHashMap<ImageRenderRequest, CompletableFuture<Path?>>()
    private val renderSession = ThreadLocal<ImageRenderSession?>()
    private val cacheCleaner = ImageCacheCleaner()

    // TODO 2.1.7+ refactoring: built-in image reader SPIs for DDS and TGA images
    // for DDS and TGA images, use image reader SPIs from TwelveMonkeys
    // see: https://github.com/haraldk/TwelveMonkeys

    // NOTE 2.1.7 MUST be lazy loaded here (avoiding eager class loading of ImageReaders and make ImageReaderCodeInjectors work as expected)
    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }
    private val tgaImageReaderSpi by lazy { TgaImageReaderSpi() }

    // NOTE 2.1.7 since IDEA 2026.1, it's necessary to set fileNameMap (or cannot render DDS / TGA images without splitting in quick doc as expected)
    private val originalFileNameMap by lazy { URLConnection.getFileNameMap() }

    private fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
    }

    private fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
    }

    private fun setFileNameMap() {
        val originalFileNameMap = originalFileNameMap // lift out or will cause SOF
        URLConnection.setFileNameMap {
            val extension = it.substringAfterLast('.').lowercase()
            when (extension) {
                "dds" -> DdsFileType.contentTypeString
                "tga" -> TgaFileType.contentTypeString
                else -> originalFileNameMap.getContentTypeFor(it)
            }
        }
    }

    private fun resetFileNameMap() {
        URLConnection.setFileNameMap(originalFileNameMap)
    }

    override fun appFrameCreated(commandLineArgs: List<String?>) {
        registerImageIOSpi()
        setFileNameMap()
        cleanupImageCacheAsync()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        registerImageIOSpi()
        setFileNameMap()
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        deregisterImageIOSpi()
        resetFileNameMap()
    }

    fun isImageFileType(fileType: FileType): Boolean {
        return fileType is ImageFileType || isExtendedImageFileType(fileType)
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        return fileType is DdsFileType || fileType is TgaFileType
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        val r = ImageSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        val r = ImageSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
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

    fun newRenderSession(scope: kotlinx.coroutines.CoroutineScope): ImageRenderSession = ImageRenderSession(scope)

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
     * 在当前渲染会话中记录渲染请求，并返回预期的目标路径。
     *
     * 如果当前没有渲染会话，则不会自动开始渲染；调用方应使用 [requestImageRender]。
     */
    fun recordRender(request: ImageRenderRequest): Path {
        renderSession.get()?.record(request)
        return request.targetPath
    }

    fun getCachedImage(request: ImageRenderRequest): Path? {
        val targetPath = request.targetPath
        return if (Files.isRegularFile(targetPath) && Files.size(targetPath) > 0) targetPath else null
    }

    /**
     * 在后台请求渲染图片。相同请求会被合并。
     */
    fun requestImageRender(request: ImageRenderRequest, project: Project): CompletableFuture<Path?> {
        renderFutures[request]?.let { return it }
        val future = CompletableFuture<Path?>()
        val previous = renderFutures.putIfAbsent(request, future)
        if (previous != null) return previous
        if (project.isDisposed) {
            renderFutures.remove(request, future)
            future.completeExceptionally(IllegalStateException("Project is disposed"))
            return future
        }

        future.whenComplete { _, _ -> renderFutures.remove(request, future) }
        val task = object : Task.Backgroundable(project, "Rendering image", true) {
            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                try {
                    future.complete(renderImage(request))
                } catch (e: ProcessCanceledException) {
                    future.completeExceptionally(e)
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
        }
        ProgressManager.getInstance().run(task)
        return future
    }

    /**
     * 同步渲染图片到目标缓存路径；如果目标已存在则直接返回。
     */
    fun renderImage(request: ImageRenderRequest): Path? {
        getCachedImage(request)?.let { return it }
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

    private fun scaleImage(image: BufferedImage, maxWidth: Int?, maxHeight: Int?): BufferedImage {
        if (maxWidth == null && maxHeight == null) return image
        val widthScale = maxWidth?.let { it.toDouble() / image.width } ?: 1.0
        val heightScale = maxHeight?.let { it.toDouble() / image.height } ?: 1.0
        val scale = min(1.0, min(widthScale, heightScale))
        if (scale >= 1.0) return image
        val width = (image.width * scale).roundToInt().coerceAtLeast(1)
        val height = (image.height * scale).roundToInt().coerceAtLeast(1)
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = result.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.drawImage(image, 0, 0, width, height, null)
        } finally {
            graphics.dispose()
        }
        return result
    }

    private fun cleanupImageCacheAsync() {
        AppExecutorUtil.getAppExecutorService().execute {
            try {
                cacheCleaner.cleanup()
            } catch (e: Exception) {
                logger.warn("Failed to clean up image cache.", e)
            }
        }
    }
}
