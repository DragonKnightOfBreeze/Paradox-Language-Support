@file:Suppress("unused")

package icu.windea.pls.images.tga

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import icu.windea.pls.core.runCatchingCancellable
import icu.windea.pls.core.util.io.asCancellable
import java.io.InputStream
import javax.imageio.ImageIO

object TgaMetadataReader {
    @RequiresReadLock(generateAssertion = false)
    fun readOrNull(file: VirtualFile): TgaMetadata? {
        return runCatchingCancellable { readInternal(file) }
            .getOrNull()
    }

    fun readOrNull(inputStream: InputStream): TgaMetadata? {
        return runCatchingCancellable { readInternal(inputStream) }
            .getOrNull()
    }

    @RequiresReadLock(generateAssertion = false)
    fun read(file: VirtualFile): TgaMetadata {
        return runCatchingCancellable { readInternal(file) }
            .getOrElse { e -> throw IllegalStateException("Cannot read tga metadata from file ${file.path}.", e) }
            ?: throw IllegalStateException("Cannot read tga metadata from file ${file.path}.")
    }

    fun read(inputStream: InputStream): TgaMetadata {
        return runCatchingCancellable { readInternal(inputStream) }
            .getOrElse { e -> throw IllegalStateException("Cannot read tga metadata from input stream.", e) }
            ?: throw IllegalStateException("Cannot read tga metadata from input stream.")
    }

    private fun readInternal(file: VirtualFile): TgaMetadata? {
        return readInternal(file.inputStream)
    }

    private fun readInternal(inputStream: InputStream): TgaMetadata? {
        // org.intellij.images.util.ImageInfoReader.read
        ImageIO.setUseCache(false)
        return inputStream.buffered().asCancellable().use { input ->
            ImageIO.createImageInputStream(input)?.use { imageInputStream ->
                // NOTE 3.0.5 Returned image readers may be multiple here, and some may be not available
                ImageIO.getImageReadersByFormatName("tga").asSequence().firstNotNullOfOrNull { imageReader ->
                    try {
                        imageReader.setInput(imageInputStream, true)
                        val width = imageReader.getWidth(0)
                        val height = imageReader.getHeight(0)
                        val bpp = imageReader.getImageTypes(0).asSequence().firstNotNullOfOrNull { it.colorModel.pixelSize } ?: -1
                        TgaMetadata(width, height, bpp)
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
