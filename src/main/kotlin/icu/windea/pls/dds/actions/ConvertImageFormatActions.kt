package icu.windea.pls.dds.actions

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.dds.support.*
import org.intellij.images.fileTypes.impl.*

class ConvertToPngAction : ConvertImageFormatAction("DDS", "PNG") {
    override fun isSourceFileType(file: VirtualFile): Boolean {
        return file.extension?.lowercase() == "dds" && file.fileType == DdsFileType
    }

    override fun getNewFileName(name: String): String {
        return name.dropLast(4) + ".png"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val t = DdsSupport.convertImageFormat(s, td, "png")
        return t?.toPsiFile(file.project)
    }
}

class ConvertToDdsAction : ConvertImageFormatAction("PNG", "DDS") {
    override fun isSourceFileType(file: VirtualFile): Boolean {
        return file.extension?.lowercase() == "png" && file.fileType == ImageFileType.INSTANCE
    }

    override fun getNewFileName(name: String): String {
        return name.dropLast(4) + ".dds"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val t = DdsSupport.convertImageFormat(s, td, "dds")
        return t?.toPsiFile(file.project)
    }
}
