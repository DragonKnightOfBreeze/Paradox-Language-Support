package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.extensions.*
import java.io.*
import java.nio.file.*

/**
 * 提供图片处理工具。用于转化、渲染特殊格式的图片，尤其是DDS图片。
 *
 * 注意：具体的操作方法可能不会再次验证工具是否可用。
 */
interface PlsImageToolProvider {
    fun isEnabled(): Boolean

    fun isSupported(): Boolean

    fun isValid(): Boolean

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
