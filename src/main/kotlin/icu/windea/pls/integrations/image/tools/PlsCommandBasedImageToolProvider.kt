package icu.windea.pls.integrations.image.tools

import com.intellij.openapi.progress.*
import icu.windea.pls.*
import org.apache.commons.io.*
import java.io.*
import java.nio.file.*
import java.util.*
import kotlin.io.path.*

abstract class PlsCommandBasedImageToolProvider : PlsImageToolProvider {
    final override fun supports(): Boolean = isAvailable() && validate()

    open fun isAvailable(): Boolean = true

    open fun validate(): Boolean = true

    open fun validatePath(path: String): Boolean = true

    final override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        try {
            val tempParentPath = PlsConstants.Paths.imagesTemp
            tempParentPath.createDirectories()
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { IOUtils.copy(inputStream, it) }
            val targetPath = convertImageFormat(path, null, null, sourceFormat, targetFormat)
            targetPath.inputStream(StandardOpenOption.READ).use { IOUtils.copy(it, outputStream) }
            path.deleteIfExists()
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    final override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        try {
            convertImageFormat(path, targetPath.parent, targetPath.name, sourceFormat, targetFormat)
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        }
    }

    abstract fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path
}
