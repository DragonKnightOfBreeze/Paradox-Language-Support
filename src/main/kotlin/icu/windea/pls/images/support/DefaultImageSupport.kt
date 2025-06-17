package icu.windea.pls.images.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import javax.imageio.*
import javax.imageio.spi.*
import kotlin.io.path.*

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
            if (e is ProcessCanceledException) throw e
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
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun doConvertImageFormat(inputStream: InputStream, outputStream: OutputStream, targetFormat: String): OutputStream {
        inputStream.buffered().use { inputStream ->
            val imageInputStream = ImageIO.createImageInputStream(inputStream)
            val imageReader = ImageIO.getImageReaders(imageInputStream).next()
            imageReader.input = imageInputStream
            val numImages = imageReader.getNumImages(false)
            if (numImages == 0) throw IllegalStateException()
            outputStream.use { outputStream ->
                repeat(numImages) { i ->
                    val image0 = imageReader.read(i)
                    ImageIO.write(image0, targetFormat, outputStream)
                    outputStream.flush()
                }
            }
        }
        return outputStream
    }
}
