package icu.windea.pls.images.supports

import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.images.ImageSupport
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.ImageToolService
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists

/**
 * 基于外部工具实现的图片支持。
 *
 * @see ImageIO
 * @see ImageIoService
 * @see ImageToolProvider
 * @see ImageToolService
 */
class ToolBasedImageSupport : ImageSupport {
    /** @see ImageToolService.convertImageFormat */
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return true
        // implementation
        return ImageToolService.getInstance().convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
    }

    /** @see ImageToolService.convertImageFormat */
    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return true
        // implementation
        try {
            return ImageToolService.getInstance().convertImageFormat(path, targetPath, sourceFormat, targetFormat)
        } catch (e: Exception) {
            runCatching { targetPath.deleteIfExists() } // ensure file deleted if operation failed
            throw e
        }
    }
}
