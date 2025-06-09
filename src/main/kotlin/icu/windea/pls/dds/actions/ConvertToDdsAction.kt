package icu.windea.pls.dds.actions

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.VfsUtil.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import org.intellij.images.fileTypes.impl.*

class ConvertToDdsAction : ConvertImageFormatAction("DDS") {
    override fun isAvailableForFile(file: VirtualFile): Boolean {
        return file.fileType == ImageFileType.INSTANCE && file.extension?.orNull() != null
    }

    override fun getNewFileName(fileName: String): String {
        return fileName.dropLast(4) + ".dds"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory, targetFileName: String): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val sf = s.extension?.orNull() ?: return null
        val path = s.toNioPath()
        val targetPath = td.toNioPath().resolve(targetFileName)
        val sourceFormat = sf.lowercase()
        DdsManager.convertImageFormat(path, targetPath, sourceFormat, "dds")
        val t = findFile(targetPath, true)
        return t?.toPsiFile(file.project)
    }
}
