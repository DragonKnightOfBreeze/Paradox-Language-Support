package icu.windea.pls.images.supports

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.images.ImageSupport
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.spi.ImageReaderSpi
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * @see ImageIO
 * @see ImageReader
 * @see ImageReaderSpi
 */
class DefaultImageSupport : ImageSupport {
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return true
        } catch (e: Exception) {
            checkCancellation(e)
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            val inputStream = path.inputStream(READ)
            val outputStream = targetPath.outputStream(WRITE, CREATE, TRUNCATE_EXISTING)
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return targetPath.exists()
        } catch (e: Exception) {
            checkCancellation(e)
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun doConvertImageFormat(inputStream: InputStream, outputStream: OutputStream, targetFormat: String): OutputStream {
        inputStream.buffered().use { inputStream ->
            ImageIO.createImageInputStream(inputStream)?.use { imageInputStream ->
                val imageReader = ImageIO.getImageReaders(imageInputStream).next()
                try {
                    imageReader.input = imageInputStream
                    // 某些图片读取器（如 JPEG）在无法预先确定帧数时会返回 -1，此时至少转换第一帧
                    val numImages = imageReader.getNumImages(false).takeIf { it > 0 } ?: 1
                    outputStream.use { outputStream ->
                        repeat(numImages) { i ->
                            val image0 = imageReader.read(i)
                            val image = toCompatibleImage(image0)
                            val written = ImageIO.write(image, targetFormat, outputStream)
                            if (!written) throw IllegalStateException("No image writer found for format: $targetFormat")
                            outputStream.flush()
                        }
                    }
                } finally {
                    imageReader.dispose()
                }
            }
        }
        return outputStream
    }

    /**
     * 将图片转换为标准颜色模型，避免 JPEG 的 CMYK 等自定义颜色模型导致 `ImageIO.write` 静默失败。
     */
    private fun toCompatibleImage(image: BufferedImage): BufferedImage {
        val type = if (image.colorModel.hasAlpha()) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        if (image.type == type) return image
        val result = BufferedImage(image.width, image.height, type)
        val graphics = result.createGraphics()
        try {
            graphics.drawImage(image, 0, 0, null)
        } finally {
            graphics.dispose()
        }
        return result
    }
}
