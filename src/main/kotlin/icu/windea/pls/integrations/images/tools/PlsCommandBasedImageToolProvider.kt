package icu.windea.pls.integrations.images.tools

import com.intellij.openapi.progress.*
import icu.windea.pls.model.constants.*
import org.apache.commons.io.*
import java.io.*
import java.nio.file.*
import java.util.*
import kotlin.io.path.*

abstract class PlsCommandBasedImageToolProvider : PlsImageToolProvider {
    final override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        val pathsToDelete = mutableSetOf<Path>()
        try {
            val tempParentPath = PlsPathConstants.imagesTemp
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            pathsToDelete.add(path)
            tempParentPath.createDirectories()
            path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                .use { IOUtils.copy(inputStream.buffered(), it) }
            val targetPath = convertImageFormat(path, null, null, sourceFormat, targetFormat)
            pathsToDelete.add(targetPath)
            targetPath.inputStream(StandardOpenOption.READ)
                .use { IOUtils.copy(it.buffered(), outputStream) }
            return true
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            throw UnsupportedOperationException(e)
        } finally {
            pathsToDelete.forEach { it.deleteIfExists() } // 确保删除临时文件
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
