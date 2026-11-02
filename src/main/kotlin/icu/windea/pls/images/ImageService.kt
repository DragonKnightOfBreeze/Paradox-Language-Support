package icu.windea.pls.images

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.tga.TgaFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.images.fileTypes.impl.ImageFileType
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile

/**
 * @see ImageSupport
 */
@Service
class ImageService {
    fun isImageFileType(fileType: FileType): Boolean {
        return isBasicImageFileType(fileType) || isExtendedImageFileType(fileType)
    }

    fun isBasicImageFileType(fileType: FileType): Boolean {
        return fileType is ImageFileType
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        return fileType is DdsFileType || fileType is TgaFileType
    }

    /**
     * 备注：调用方应确保不在 EDT/readAction 中等待该操作。
     *
     * @see ImageSupport.convertImageFormat
     */
    @Throws(UnsupportedOperationException::class)
    suspend fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return
        // 3.0.5 try each implementation and apply fallback
        withContext(Dispatchers.IO) { doConvertImageFormat(inputStream, outputStream, sourceFormat, targetFormat) }
    }

    /**
     * 备注：调用方应确保不在 EDT/readAction 中等待该操作。
     *
     * @see ImageSupport.convertImageFormat
     */
    @Throws(UnsupportedOperationException::class)
    suspend fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return
        // 3.0.5 try each implementation and apply fallback
        withContext(Dispatchers.IO) { doConvertImageFormat(path, targetPath, sourceFormat, targetFormat) }
    }

    private suspend fun doConvertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
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

    private suspend fun doConvertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        var converted = false
        var lastError: Exception? = null
        for (support in ImageSupport.EP_NAME.extensionList) {
            converted = try {
                support.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
            } catch (e: Exception) {
                checkCancellation(e)
                lastError = e
                false
            }
            converted = converted && checkTargetPath(targetPath)
            if (converted) break
        }
        if (!converted) throwException(lastError)
    }

    private fun checkTargetPath(targetPath: Path): Boolean {
        if (targetPath.isRegularFile() && targetPath.fileSize() > 0) return true
        runCatching { targetPath.deleteIfExists() }
        return false
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
