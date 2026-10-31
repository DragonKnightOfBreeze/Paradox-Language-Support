package icu.windea.pls.integrations.images

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.ExceptionUtil
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.io.ImageIoService
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

@Service
class ImageToolService : Disposable {
    /** @see ImageToolProvider */
    fun findTool(): ImageToolProvider? {
        return ImageToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    /** @see ImageToolProvider.read */
    fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage? {
        // implementation
        val tool = findTool() ?: return null // skip
        if (warnIfIncorrectContext(tool)) return null // skip
        try {
            val result = tool.read(imageIndex, param, stream, sourceFormat, targetFormat)
            logOperation(tool) { "read (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            return result
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "read (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            throw e
        }
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return true
        // implementation
        val tool = findTool() ?: return false // skip
        if (warnIfIncorrectContext(tool)) return false // skip
        warnIfIncorrectContext(tool)
        try {
            tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
            logOperation(tool) { "convert image format (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            return true
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "convert image format (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            throw e
        }
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return true
        // implementation
        val tool = findTool() ?: return false // skip
        if (warnIfIncorrectContext(tool)) return false // skip
        try {
            tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
            logOperation(tool) { "convert image format (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            return true
        } catch (e: Exception) {
            checkCancellation(e)
            logOperation(tool, e) { "convert image format (sourceFormat=$sourceFormat, targetFormat=$targetFormat)" }
            throw e
        }
    }

    private fun warnIfIncorrectContext(tool: ImageToolProvider): Boolean {
        // 3.0.5 warn if current context is incorrect (EDT or readAction), and skip operation
        val application = ApplicationManager.getApplication()
        val message = when {
            application.isDispatchThread -> "on EDT"
            application.holdsReadLock() -> "under readAction"
            else -> return false
        }
        if (!warnedStacks.add(ExceptionUtil.currentStackTrace())) return true
        logger.warn("[${tool.name}] External image tool is used $message.")
        return true
    }

    private fun logOperation(tool: ImageToolProvider, error: Throwable? = null, operation: () -> String) {
        if (error != null) {
            if (!warnedStacks.add(ExceptionUtil.currentStackTrace())) return
            logger.warn("[${tool.name}] Execute image tool failed. Operation: ${operation()}.")
        } else {
            if (!logger.isDebugEnabled) return
            logger.debug("[${tool.name}] Execute image tool success. Operation: ${operation}.")
        }
    }

    override fun dispose() {
        warnedStacks.clear()
    }

    companion object {
        private val logger = logger<ImageToolService>()
        private val warnedStacks = ConcurrentHashMap.newKeySet<String>()

        @JvmStatic
        fun getInstance(): ImageToolService = service()
    }
}
