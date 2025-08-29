package icu.windea.pls.images.support

import com.intellij.openapi.extensions.ExtensionPointName
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

interface ImageSupport {
    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean

    @Throws(UnsupportedOperationException::class)
    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ImageSupport>("icu.windea.pls.images.support")
    }
}
