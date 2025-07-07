package icu.windea.pls.integrations.images

import icu.windea.pls.integrations.images.tools.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import javax.imageio.*
import javax.imageio.stream.*

object PlsImageManager {
    fun findTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.EP_NAME.extensionList.findLast { it.isAvailable() }
    }

    fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage? {
        val tool = findTool() ?: return null
        val image = tool.read(imageIndex, param, stream, sourceFormat, targetFormat)
        return image
    }

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
        return true
    }

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        return true
    }
}
