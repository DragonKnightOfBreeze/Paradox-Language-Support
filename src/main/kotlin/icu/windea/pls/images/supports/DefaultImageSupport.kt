package icu.windea.pls.images.supports

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.images.ImageSupport
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

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
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return true
        try {
            ImageIoService.convertImageFormat(inputStream, outputStream, targetFormat)
            return true
        } catch (e: Exception) {
            checkCancellation(e)
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return true
        try {
            ImageIoService.convertImageFormat(path, targetPath, targetFormat)
            return targetPath.exists()
        } catch (e: Exception) {
            runCatching { targetPath.deleteIfExists() }
            checkCancellation(e)
            thisLogger().warn(e)
            throw UnsupportedOperationException(e)
        }
    }
}
