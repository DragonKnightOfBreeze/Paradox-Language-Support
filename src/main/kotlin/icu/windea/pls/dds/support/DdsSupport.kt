package icu.windea.pls.dds.support

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*
import java.io.*
import javax.imageio.*

/**
 * 用于提供对DDS图片的支持，包括获取元信息、渲染图片、转化图片格式等。
 */
interface DdsSupport {
    fun getMetadata(file: VirtualFile): DdsMetadata?

    fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader?

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): OutputStream?

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFileName: String, sourceFormat: String, targetFormat: String): VirtualFile?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<DdsSupport>("icu.windea.pls.dds.support")

        fun getMetadata(file: VirtualFile): DdsMetadata? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.getMetadata(file)
            }
        }

        fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.createImageReader(extension, spi)
            }
        }

        @Throws(UnsupportedOperationException::class)
        fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): OutputStream? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
            }
        }

        @Throws(UnsupportedOperationException::class)
        fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFileName: String, sourceFormat: String, targetFormat: String): VirtualFile? {
            return EP_NAME.extensionList.firstNotNullOfOrNull {
                it.convertImageFormat(file, targetDirectory, targetFileName, sourceFormat, targetFormat)
            }
        }
    }
}
