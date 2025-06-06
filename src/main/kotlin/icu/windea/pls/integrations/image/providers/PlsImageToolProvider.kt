package icu.windea.pls.integrations.image.providers

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import java.io.*
import java.nio.file.*

/**
 * 提供图片处理工具，用于转化、渲染特殊格式的图片，尤其是DDS图片。
 *
 * 注意：具体的操作方法不会再次验证工具是否受支持（[supports]）。
 */
interface PlsImageToolProvider {
    fun isEnabled(): Boolean

    fun supports(): Boolean

    fun open(file: VirtualFile): Boolean

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsImageToolProvider>("icu.windea.pls.integrations.imageToolProvider")
    }
}
