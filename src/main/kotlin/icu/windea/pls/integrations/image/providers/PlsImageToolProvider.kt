package icu.windea.pls.integrations.image.providers

import com.intellij.openapi.extensions.ExtensionPointName
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * 提供图片处理工具，用于转化、渲染特殊格式的图片，尤其是DDS图片。
 */
interface PlsImageToolProvider {
    fun supports(): Boolean

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
