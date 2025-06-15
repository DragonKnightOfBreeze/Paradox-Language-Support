package icu.windea.pls.dds.support

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.io.*
import icu.windea.pls.dds.*
import icu.windea.pls.integrations.image.*
import icu.windea.pls.integrations.image.tools.*
import icu.windea.pls.lang.*
import io.github.ititus.ddsiio.*
import org.apache.commons.io.*
import org.apache.commons.io.file.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.nio.file.StandardOpenOption.*
import java.util.*
import javax.imageio.*
import javax.imageio.stream.*
import kotlin.io.path.*

/**
 * 用于基于图片工具扩展点，进行相关的图片处理操作，例如将DDS图片转化为PNG图片。
 *
 * @see PlsImageManager
 * @see PlsImageToolProvider
 */
class ToolBasedDdsSupport : DdsSupport {
    private val logger = thisLogger()

    override fun getMetadata(file: VirtualFile): DdsMetadata? {
        return null //unnecessary to implement
    }

    override fun createImageReader(extension: Any?, spi: DdsImageReaderSpi): ImageReader? {
        if (PlsImageManager.findTool() == null) return null
        return ImageReader(spi, this)
    }

    class ImageReader(
        spi: DdsImageReaderSpi,
        private val support: ToolBasedDdsSupport
    ) : DdsImageReader(spi) {
        val stream: ImageInputStream? by memberProperty<DdsImageReader, _>("stream")

        override fun read(imageIndex: Int, param: ImageReadParam?): BufferedImage {
            return doRead(imageIndex, param)
        }

        private fun doRead(imageIndex: Int, param: ImageReadParam?): BufferedImage {
            val stream = stream
            val image = try {
                doReadFromStream(stream)
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                thisLogger().warn(e)
                null
            }
            return image ?: super.read(imageIndex, param)
        }

        private fun doReadFromStream(stream: ImageInputStream?): BufferedImage? {
            if (stream == null) return null
            val inputStream = ImageInputStreamAdapter(stream)
            val outputStream = ByteArrayOutputStream()
            val r = support.convertImageFormat(inputStream.buffered(), outputStream, "dds", "png")
            if (!r) return null
            val input = ByteArrayInputStream(outputStream.toByteArray())
            return ImageIO.read(input.buffered())
        }
    }

    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        return PlsImageManager.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        return PlsImageManager.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
    }
}
