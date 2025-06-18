package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.extensions.*
import java.io.*
import java.nio.file.*

/**
 * 提供图片处理工具。目前仅用于转换图片格式（PNG、DDS、TGA）。
 *
 * 注意：具体的操作方法可能不会再次验证工具是否可用。
 */
interface PlsImageToolProvider {
    fun isAvailable() = isEnabled() && isSupported() && isValid()

    fun isEnabled(): Boolean

    fun isSupported(): Boolean

    fun isValid(): Boolean

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
