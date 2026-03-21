package icu.windea.pls.images.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import icu.windea.pls.images.ImageSupport
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.ImageToolService
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * @see ImageToolProvider
 * @see ImageToolService
 */
class ToolBasedImageSupport : ImageSupport {
    /** @see ImageToolService.convertImageFormat */
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return ImageToolService.getInstance().convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    /** @see ImageToolService.convertImageFormat */
    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return ImageToolService.getInstance().convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }
}
