package icu.windea.pls.images.tga

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.runCatchingCancelable
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

//org.intellij.images.util.ImageInfoReader.read

object TgaManager {
    fun getMetadata(file: VirtualFile): TgaMetadata? {
        return readMetadata(ByteArrayInputStream(file.contentsToByteArray()))
    }

    private fun readMetadata(input: Any): TgaMetadata? {
        ImageIO.setUseCache(false) //same as org.intellij.images.util.ImageInfoReader.read
        runCatchingCancelable {
            ImageIO.createImageInputStream(input).use { iis ->
                ImageIO.getImageReaders(iis).forEach { reader ->
                    reader.setInput(iis, true)
                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)
                    val it2 = reader.getImageTypes(0)
                    val bpp = runCatchingCancelable {
                        reader.getImageTypes(0).asSequence().firstNotNullOfOrNull { it.colorModel.pixelSize }
                    }.getOrNull() ?: -1
                    return TgaMetadata(width, height, bpp)
                }
            }
        }
        return null
    }
}
