package icu.windea.pls.images

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.dds.DdsMetadataIndex
import icu.windea.pls.images.dds.DdsMetadataReader
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.images.tga.TgaMetadataIndex
import icu.windea.pls.images.tga.TgaMetadataReader
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream

/**
 * 统一获取图片的原始元数据。
 */
@Service
class ImageMetadataService : Disposable {
    private data class CacheKey(
        val path: String,
        val length: Long,
        val modificationStamp: Long,
    )

    // TODO 3.0.5 后续可以按需调整为更细粒度的缓存策略
    private val cache = CacheBuilder("maximumSize=4096,expireAfterAccess=10m")
        .build<CacheKey, ImageMetadata>()

    @RequiresReadLock(generateAssertion = false)
    fun getMetadata(file: VirtualFile, project: Project? = null): ImageMetadata? {
        val key = CacheKey(file.path, file.length, file.modificationStamp)
        cache.getIfPresent(key)?.let { return it }
        val metadata = runCatchingCancelable { doGetMetadata(file, project) }
            .onFailure { thisLogger().warn(it) }
            .getOrNull()
        if (metadata != null) cache.put(key, metadata)
        return metadata
    }

    fun getMetadata(path: Path): ImageMetadata? {
        val key = runCatchingCancelable {
            CacheKey(path.toAbsolutePath().normalize().toString(), Files.size(path), Files.getLastModifiedTime(path).toMillis())
        }.getOrNull() ?: return null
        cache.getIfPresent(key)?.let { return it }
        val metadata = runCatchingCancelable { readMetadata(path.inputStream()) }
            .onFailure { thisLogger().warn(it) }
            .getOrNull()
        if (metadata != null) cache.put(key, metadata)
        return metadata
    }

    private fun doGetMetadata(file: VirtualFile, project: Project?): ImageMetadata? {
        return when (file.fileType) {
            DdsFileType -> {
                val metadata = if (project != null && !project.isDisposed) {
                    DdsMetadataIndex.getInstance().getMetadata(file, project)
                } else {
                    DdsMetadataReader.readOrNull(file)
                }
                metadata?.let { ImageMetadata(it.width, it.height) }
            }
            TgaFileType -> {
                val metadata = if (project != null && !project.isDisposed) {
                    TgaMetadataIndex.getInstance().getMetadata(file, project)
                } else {
                    TgaMetadataReader.readOrNull(file)
                }
                metadata?.let { ImageMetadata(it.width, it.height) }
            }
            else -> {
                readMetadata(file.inputStream)
            }
        }
    }

    private fun readMetadata(inputStream: InputStream): ImageMetadata? {
        return inputStream.buffered().use { input ->
            ImageIO.createImageInputStream(input)?.use { imageInputStream ->
                val imageReader = ImageIO.getImageReaders(imageInputStream).asSequence().firstOrNull() ?: return null
                try {
                    imageReader.input = imageInputStream
                    val width = imageReader.getWidth(0)
                    val height = imageReader.getHeight(0)
                    ImageMetadata(width, height)
                } finally {
                    imageReader.dispose()
                }
            }
        }
    }

    override fun dispose() {
        cache.invalidateAll()
        cache.cleanUp()
    }

    companion object {
        @JvmStatic
        fun getInstance(): ImageMetadataService = service()
    }
}
