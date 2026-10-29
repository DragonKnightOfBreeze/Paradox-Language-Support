@file:Suppress("unused")

package icu.windea.pls.images.tga

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import icu.windea.pls.core.runCatchingCancellable
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO.*

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
    }

    fun read(inputStream: InputStream): TgaMetadata {
        return runCatchingCancellable { readInternal(inputStream) }
            .getOrElse { e -> throw IllegalStateException("Cannot read tga metadata from input stream.", e) }
    }

    private fun readInternal(file: VirtualFile): TgaMetadata {
        return readInternal(file.inputStream)
    }

    private fun readInternal(inputStream: InputStream): TgaMetadata {
        // org.intellij.images.util.ImageInfoReader.read
        setUseCache(false)
        createImageInputStream(inputStream).use { iis ->
            getImageReaders(iis).forEach { reader ->
                reader.setInput(iis, true)
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                val bpp = reader.getImageTypes(0).asSequence().firstNotNullOfOrNull { it.colorModel.pixelSize } ?: -1
                return TgaMetadata(width, height, bpp)
            }
        }
        throw IOException("No image reader found.")
    }
}
