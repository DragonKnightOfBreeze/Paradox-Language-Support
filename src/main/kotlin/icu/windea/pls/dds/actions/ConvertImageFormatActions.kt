package icu.windea.pls.dds.actions

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.dds.support.*
import org.intellij.images.fileTypes.impl.*

class ConvertToPngAction : ConvertImageFormatAction("PNG") {
    override fun isAvailableForFile(file: VirtualFile): Boolean {
        return file.fileType == DdsFileType
    }

    override fun getNewFileName(fileName: String): String {
        return fileName.dropLast(4) + ".png"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val t = DdsManager.convertImageFormat(s, td, "png")
        return t?.toPsiFile(file.project)
    }
}

class ConvertToDdsAction : ConvertImageFormatAction("DDS") {
    override fun isAvailableForFile(file: VirtualFile): Boolean {
        return file.fileType == ImageFileType.INSTANCE
    }

    override fun getNewFileName(fileName: String): String {
        return fileName.dropLast(4) + ".dds"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val t = DdsManager.convertImageFormat(s, td, "dds")
        return t?.toPsiFile(file.project)
    }
}
