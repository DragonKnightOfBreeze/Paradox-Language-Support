package icu.windea.pls.images.supports

import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.images.ImageSupport
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists

/**
 * 基于 [TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys) 实现的图片支持。
 *
 * 注意：存在一些限制。例如，不支持写入 DDS 图片，不支持转换为 DDS 图片，也不支持部分新版的 DDS 扩展格式（如 BC6H/BC7）。
 *
 * @see ImageIO
 * @see ImageIoService
 */
class DefaultImageSupport : ImageSupport {
    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return true
        // implementation
        ImageIoService.convertImageFormat(inputStream, outputStream, targetFormat)
        return true
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return true
        // implementation
        try {
            ImageIoService.convertImageFormat(path, targetPath, targetFormat)
            return true
        } catch (e: Exception) {
            runCatching { targetPath.deleteIfExists() } // ensure file deleted if operation failed
            throw e
        }
    }
}
