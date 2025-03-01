package icu.windea.pls.dds.support

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*
import java.io.*
import java.nio.file.*
import javax.imageio.*

/**
 * 用于提供对DDS图片的支持，包括获取元信息、渲染图片、转化图片格式等。
 */
interface DdsSupport {
    fun getMetadata(file: VirtualFile): DdsMetadata?

    fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader?

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<DdsSupport>("icu.windea.pls.dds.support")
    }
}
