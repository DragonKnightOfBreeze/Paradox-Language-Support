package icu.windea.pls.integrations.images

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
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
        @JvmStatic
        fun getInstance(): ImageToolService = service()
    }
}
