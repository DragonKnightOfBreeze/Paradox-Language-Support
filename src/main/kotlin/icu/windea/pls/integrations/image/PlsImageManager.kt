package icu.windea.pls.integrations.image

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.dds.*
import icu.windea.pls.integrations.image.providers.*
import org.intellij.images.fileTypes.impl.*
import java.io.*
import java.nio.file.*

object PlsImageManager {
    fun isImageFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isImageFile(vFile)
    }

    fun isImageFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType !is ImageFileType && fileType !is DdsFileType) return false
        val extension = file.extension?.lowercase() ?: return false
        if (extension !in PlsConstants.imageFileExtensions) return false
        return true
    }

    fun findTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.EP_NAME.extensionList.findLast { it.isEnabled() && it.supports() }
    }

    fun findRequiredTool(): PlsImageToolProvider {
        return findTool() ?: throw UnsupportedOperationException("Unsupported: No available image tool found.")
    }

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        return tool.convertImageFormat(inputStream, outputStream, sourceFormat, targetFormat)
    }

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        val tool = findTool() ?: return false
        return tool.convertImageFormat(path, targetPath, sourceFormat, targetFormat)
    }
}
