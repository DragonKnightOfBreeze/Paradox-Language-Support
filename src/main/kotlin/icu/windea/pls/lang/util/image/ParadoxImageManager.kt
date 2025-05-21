package icu.windea.pls.lang.util.image

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.dds.*
import org.intellij.images.fileTypes.impl.*

object ParadoxImageManager {
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
}
