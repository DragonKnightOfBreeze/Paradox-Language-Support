package icu.windea.pls.images

import com.intellij.openapi.fileTypes.*
import com.twelvemonkeys.imageio.plugins.dds.*
import com.twelvemonkeys.imageio.plugins.tga.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.images.support.*
import icu.windea.pls.images.tga.*
import org.intellij.images.fileTypes.impl.*
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

    private val ddsImageReaderSpi by lazy { DDSImageReaderSpi() }
    private val tgaImageReaderSpi by lazy { TGAImageReaderSpi() }

    fun registerImageIOSpi() {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().registerServiceProvider(tgaImageReaderSpi)
    }

    fun deregisterImageIOSpi() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        IIORegistry.getDefaultInstance().deregisterServiceProvider(tgaImageReaderSpi)
    }
}
