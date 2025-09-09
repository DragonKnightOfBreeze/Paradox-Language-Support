package icu.windea.pls.images.tga

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.runCatchingCancelable
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO.createImageInputStream
import javax.imageio.ImageIO.getImageReaders
import javax.imageio.ImageIO.setUseCache

object TgaMetadataReader {
    fun read(file: VirtualFile): TgaMetadata? {
        return runCatchingCancelable { doRead(file) }.getOrNull()
    }

    private fun doRead(file: VirtualFile): TgaMetadata? {
        // org.intellij.images.util.ImageInfoReader.read
        setUseCache(false)
        val inputStream = ByteArrayInputStream(file.contentsToByteArray())
        createImageInputStream(inputStream).use { iis ->
            getImageReaders(iis).forEach { reader ->
                reader.setInput(iis, true)
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                val bpp = reader.getImageTypes(0).asSequence().firstNotNullOfOrNull { it.colorModel.pixelSize } ?: -1
                return TgaMetadata(width, height, bpp)
            }
        }
        return null
    }
}
