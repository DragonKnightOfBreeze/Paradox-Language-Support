package icu.windea.pls.integrations.images

import icu.windea.pls.integrations.images.tools.*
import java.io.*
import java.nio.file.*

object PlsImageManager {
    fun findTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    fun findRequiredTool(): PlsImageToolProvider {
        return findTool() ?: throw UnsupportedOperationException("Unsupported: No available image tool found.")
    }

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        return tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
    }

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        return tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
    }
}
