package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.extensions.*
import java.io.*
import java.nio.file.*

/**
 * 提供图片处理工具。目前仅用于转换图片格式（PNG、DDS、TGA）。
 */
interface PlsImageToolProvider {
    fun isAvailable(): Boolean

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String)

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
