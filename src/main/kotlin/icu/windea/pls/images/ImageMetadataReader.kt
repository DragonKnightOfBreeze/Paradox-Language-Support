package icu.windea.pls.images

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.io.asCancellable
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.dds.DdsMetadataIndex
import icu.windea.pls.images.dds.DdsMetadataReader
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.images.tga.TgaMetadataIndex
import icu.windea.pls.images.tga.TgaMetadataReader
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream

/**
 * 图片元数据读取器。
 *
 * 优先使用 DDS/TGA 的自定义读取器或索引，其他图片类型使用 [ImageIO]。
 */
object ImageMetadataReader {
    fun read(file: VirtualFile, project: Project? = null): ImageMetadata? {
        val result = when (file.fileType) {
            DdsFileType -> {
                val metadata = if (project != null && !project.isDisposed) {
                    runCatching { DdsMetadataIndex.getInstance().getMetadata(file, project) }.getOrNull()
                } else {
                    null
                } ?: DdsMetadataReader.readOrNull(file)
                metadata?.let { ImageMetadata(it.width, it.height) }
            }
            TgaFileType -> {
                val metadata = if (project != null && !project.isDisposed) {
                    runCatching { TgaMetadataIndex.getInstance().getMetadata(file, project) }.getOrNull()
                } else {
                    null
                } ?: TgaMetadataReader.readOrNull(file)
                metadata?.let { ImageMetadata(it.width, it.height) }
            }
            else -> {
                read(file.inputStream)
            }
        }
        return result ?: read(file.inputStream)
    }

    fun read(path: Path): ImageMetadata? {
        return path.inputStream().use { read(it) }
    }

    fun read(inputStream: InputStream): ImageMetadata? {
        // org.intellij.images.util.ImageInfoReader.read
        ImageIO.setUseCache(false)
        return inputStream.buffered().asCancellable().use { input ->
            ImageIO.createImageInputStream(input)?.use { imageInputStream ->
                // NOTE 3.0.5 Returned image readers may be multiple here, and some may be not available
                ImageIO.getImageReaders(imageInputStream).asSequence().firstNotNullOfOrNull { imageReader ->
                    try {
                        imageReader.setInput(imageInputStream, true)
                        val width = imageReader.getWidth(0)
                        val height = imageReader.getHeight(0)
                        ImageMetadata(width, height)
                    } catch (_: Exception) {
                        null
                    } finally {
                        imageReader.dispose()
                    }
                }
            }
        }
    }
}
