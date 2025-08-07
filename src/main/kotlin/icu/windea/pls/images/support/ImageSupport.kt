package icu.windea.pls.images.support

import com.intellij.openapi.extensions.*
import java.io.*
import java.nio.file.*

interface ImageSupport {
    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ImageSupport>("icu.windea.pls.images.support")
    }
}
