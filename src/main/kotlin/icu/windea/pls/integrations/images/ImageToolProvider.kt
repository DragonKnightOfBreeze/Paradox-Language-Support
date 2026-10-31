package icu.windea.pls.integrations.images

import com.intellij.openapi.extensions.ExtensionPointName
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream

/**
 * 提供图片工具。用于处理图片，例如预览、渲染、读写以及转换图片格式。
 */
interface ImageToolProvider {
    val name: String

    fun isAvailable(): Boolean

    /** @see javax.imageio.ImageReader.read */
    fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String)

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String)

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
