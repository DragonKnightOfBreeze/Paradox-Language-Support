package icu.windea.pls.images.dds

import com.intellij.openapi.vfs.*
import icu.windea.pls.images.dds.support.*
import java.io.*
import java.nio.file.*
import javax.imageio.*

object DdsManager {
    fun getMetadata(file: VirtualFile): DdsMetadata? {
        return DdsSupport.EP_NAME.extensionList.firstNotNullOfOrNull {
            it.getMetadata(file)
        }
    }

    fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
        return DdsSupport.EP_NAME.extensionList.firstNotNullOfOrNull {
            it.createImageReader(extension, spi)
        }
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        val r = DdsSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        val r = DdsSupport.EP_NAME.extensionList.any {
            it.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }
}
