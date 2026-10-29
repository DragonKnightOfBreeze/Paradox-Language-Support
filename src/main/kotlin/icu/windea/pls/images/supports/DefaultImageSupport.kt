package icu.windea.pls.images.supports

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.io.copyImageFile
import icu.windea.pls.core.util.io.copyImageStream
import icu.windea.pls.core.util.io.getCompatibleNumImages
import icu.windea.pls.core.util.io.isSameImageFormat
import icu.windea.pls.core.util.io.toCompatibleImage
import icu.windea.pls.images.ImageSupport
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.spi.ImageReaderSpi
import kotlin.io.path.deleteIfExists
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
        if (isSameImageFormat(sourceFormat, targetFormat)) {
            copyImageStream(inputStream, outputStream)
            return true
        }
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
        if (isSameImageFormat(sourceFormat, targetFormat)) {
            copyImageFile(path, targetPath)
            return true
        }
        try {
            val inputStream = path.inputStream(READ)
            val outputStream = targetPath.outputStream(WRITE, CREATE, TRUNCATE_EXISTING)
            doConvertImageFormat(inputStream, outputStream, targetFormat)
            return targetPath.exists()
        } catch (e: Exception) {
            runCatching { targetPath.deleteIfExists() }
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
                    val numImages = imageReader.getCompatibleNumImages()
                    outputStream.use { outputStream ->
                        repeat(numImages) { i ->
                            val image0 = imageReader.read(i)
                            val image = image0.toCompatibleImage(targetFormat)
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
}
