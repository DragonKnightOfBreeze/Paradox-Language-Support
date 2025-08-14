package icu.windea.pls.images

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.images.spi.*
import icu.windea.pls.images.support.*
import icu.windea.pls.images.tga.*
import icu.windea.pls.model.*
import org.intellij.images.fileTypes.impl.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import javax.imageio.spi.*

object ImageManager {
    fun isImageFileType(fileType: FileType): Boolean {
        return fileType !is ImageFileType || isExtendedImageFileType(fileType)
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        if (fileType !is DdsFileType && fileType !is TgaFileType) return false
        return true
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

    // 对于 DDS 和 TGA 图片，统一使用 TwelveMonkeys 提供的 SPI
    // 参见：https://github.com/haraldk/TwelveMonkeys

    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }
    private val tgaImageReaderSpi by lazy { TgaImageReaderSpi() }

    fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
    }

    fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
    }

    //utility methods

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
}
