package icu.windea.pls.images.support

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import icu.windea.pls.integrations.images.*
import icu.windea.pls.integrations.images.tools.*
import java.io.*
import java.nio.file.*

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
