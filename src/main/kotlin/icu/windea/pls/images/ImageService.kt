package icu.windea.pls.images

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.tga.TgaFileType
import org.intellij.images.fileTypes.impl.ImageFileType
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class ImageService {
    fun isBasicImageFileType(fileType: FileType): Boolean {
        return fileType is ImageFileType || isImageFileType(fileType)
    }

    fun isImageFileType(fileType: FileType): Boolean {
        return fileType is DdsFileType || fileType is TgaFileType
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return
        // 3.0.5 try each implementation and apply fallback
        var converted = false
        var lastError: Exception? = null
        for (support in ImageSupport.EP_NAME.extensionList) {
            converted = try {
                support.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
            } catch (e: Exception) {
                checkCancellation(e)
                lastError = e
                false
            }
            if (converted) break
        }
        if (!converted) throwException(lastError)
    }

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return
        // 3.0.5 try each implementation and apply fallback
        var converted = false
        var lastError: Exception? = null
        for (support in ImageSupport.EP_NAME.extensionList) {
            converted = try {
                support.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
            } catch (e: Exception) {
                lastError = e
                false
            }
            if (converted && Files.isRegularFile(targetPath) && Files.size(targetPath) > 0) break
            runCatching { Files.deleteIfExists(targetPath) }
            converted = false
        }
        if (!converted) throwException(lastError)
    }

    private fun throwException(lastError: Exception?): Nothing {
        if (lastError is UnsupportedOperationException) throw lastError
        throw UnsupportedOperationException(lastError)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ImageService = service()
    }
}
