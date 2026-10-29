package icu.windea.pls.integrations.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.ExceptionUtil
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

@Service
class ImageToolService {
    /** @see ImageToolProvider */
    fun findTool(): ImageToolProvider? {
        return ImageToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    /** @see ImageToolProvider.read */
    fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage? {
        if (warnIfIncorrectContext()) return null
        val tool = findTool() ?: return null
        val image = tool.read(imageIndex, param, stream, sourceFormat, targetFormat)
        return image
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        if (warnIfIncorrectContext()) return false
        val tool = findTool() ?: return false
        tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        return true
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        if (warnIfIncorrectContext()) return false
        val tool = findTool() ?: return false
        tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        return true
    }

    private fun warnIfIncorrectContext(): Boolean {
        // 3.0.5 warn if current context is incorrect (EDT or readAction), and skip operation
        val application = ApplicationManager.getApplication()
        val message = when {
            application.isDispatchThread -> "on EDT"
            application.holdsReadLock() -> "under readAction"
            else -> return false
        }
        val stack = ExceptionUtil.currentStackTrace()
        if (!warnedStacks.add(stack)) return true
        logger.warn("External image tool is used $message. Skip and fallback to default implementation.")
        return true
    }

    companion object {
        private val logger = logger<ImageToolService>()
        private val warnedStacks = ConcurrentHashMap.newKeySet<String>()

        @JvmStatic
        fun getInstance(): ImageToolService = service()
    }
}
