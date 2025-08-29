package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.extensions.ExtensionPointName
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream

/**
 * 提供图片处理工具。用于预览与渲染图片，以及转换图片格式。
 */
interface PlsImageToolProvider {
    fun isAvailable(): Boolean

    /**
     * @see ImageReader.read
     */
    fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String)

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
