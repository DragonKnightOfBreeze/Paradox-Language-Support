package icu.windea.pls.dds.support

import com.intellij.openapi.vfs.*
import icu.windea.pls.dds.*
import icu.windea.pls.dds.support.DdsSupport.INSTANCE.EP_NAME
import java.io.*
import java.nio.file.*
import javax.imageio.*
import javax.imageio.spi.*

object DdsManager {
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
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        val r = EP_NAME.extensionList.any {
            it.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        val r = EP_NAME.extensionList.any {
            it.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        }
        if (!r) throw UnsupportedOperationException()
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(file: VirtualFile, targetDirectory: VirtualFile, targetFileName: String, sourceFormat: String, targetFormat: String): VirtualFile? {
        val path = file.toNioPath()
        val targetPath = targetDirectory.toNioPath().resolve(targetFileName)
        convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        return VfsUtil.findFile(targetPath, true)
    }
}
