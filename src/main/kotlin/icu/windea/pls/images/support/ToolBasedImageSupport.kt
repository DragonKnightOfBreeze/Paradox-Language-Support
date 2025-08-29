package icu.windea.pls.images.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import icu.windea.pls.integrations.images.PlsImageManager
import icu.windea.pls.integrations.images.tools.PlsImageToolProvider
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * @see PlsImageManager
 * @see PlsImageToolProvider
 */
class ToolBasedImageSupport : ImageSupport {
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return PlsImageManager.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            return PlsImageManager.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }
}
