package icu.windea.pls.images.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import icu.windea.pls.integrations.images.ImageIntegrationManager
import icu.windea.pls.integrations.images.tools.ImageToolProvider
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * @see ImageIntegrationManager
 * @see ImageToolProvider
 */
class ToolBasedImageSupport : ImageSupport {
    /** @see ImageIntegrationManager.convertImageFormat */
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return ImageIntegrationManager.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    /** @see ImageIntegrationManager.convertImageFormat */
    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return ImageIntegrationManager.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }
}
