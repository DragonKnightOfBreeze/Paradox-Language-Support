package icu.windea.pls.integrations.images

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
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
        // NOTE 3.0.5 图片读取器可能被 EDT 或读操作触发；此时跳过外部工具，交回内置实现
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread || application.isReadAccessAllowed) {
            logger.warn("Skip external image tool in EDT or read action. (source format: $sourceFormat)")
            return null
        }
        val tool = findTool() ?: return null
        val image = tool.read(imageIndex, param, stream, sourceFormat, targetFormat)
        return image
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        return true
    }

    /** @see ImageToolProvider.convertImageFormat */
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        return true
    }

    companion object {
        private val logger = logger<ImageToolService>()

        @JvmStatic
        fun getInstance(): ImageToolService = service()
    }
}
