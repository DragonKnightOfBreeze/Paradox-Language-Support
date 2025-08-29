package icu.windea.pls.integrations.images.tools

import icu.windea.pls.core.io.ImageInputStreamAdapter
import icu.windea.pls.model.constants.PlsPathConstants
import org.apache.commons.io.IOUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.outputStream

abstract class PlsCommandBasedImageToolProvider : PlsImageToolProvider {
    final override fun isAvailable() = isEnabled() && isSupported() && isValid()

    abstract fun isEnabled(): Boolean

    abstract fun isSupported(): Boolean

    abstract fun isValid(): Boolean

    abstract fun validatePath(path: String): Boolean

    override fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage {
        val inputStream = ImageInputStreamAdapter(stream)
        val outputStream = ByteArrayOutputStream()
        convertImageFormat(inputStream.buffered(), outputStream, sourceFormat, targetFormat)
        val input = ByteArrayInputStream(outputStream.toByteArray())
        return ImageIO.read(input.buffered())
    }

    final override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        val pathsToDelete = mutableSetOf<Path>()
        try {
            val tempParentPath = PlsPathConstants.imagesTemp
            tempParentPath.createDirectories()
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            pathsToDelete.add(path)
            path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                .use { IOUtils.copy(inputStream.buffered(), it) }
            val targetPath = convertImageFormat(path, null, null, sourceFormat, targetFormat)
            pathsToDelete.add(targetPath)
            targetPath.inputStream(StandardOpenOption.READ)
                .use { IOUtils.copy(it.buffered(), outputStream) }
        } finally {
            pathsToDelete.forEach { it.deleteIfExists() } // 确保删除临时文件
        }
    }

    final override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        convertImageFormat(path, targetPath.parent, targetPath.name, sourceFormat, targetFormat)
    }

    abstract fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path
}
