package icu.windea.pls.integrations.images.tools

import icu.windea.pls.core.io.*
import icu.windea.pls.model.constants.*
import org.apache.commons.io.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.util.*
import javax.imageio.*
import javax.imageio.stream.*
import kotlin.io.path.*

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
