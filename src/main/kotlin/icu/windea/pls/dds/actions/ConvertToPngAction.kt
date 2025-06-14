package icu.windea.pls.dds.actions

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.VfsUtil.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*

class ConvertToPngAction : ConvertImageFormatAction("PNG") {
    override fun isAvailableForFile(file: VirtualFile): Boolean {
        return file.fileType == DdsFileType
    }

    override fun getNewFileName(fileName: String): String {
        return fileName.dropLast(4) + ".png"
    }

    override fun convertImageFormat(file: PsiFile, targetDirectory: PsiDirectory, targetFileName: String): PsiFile? {
        val s = file.virtualFile ?: return null
        val td = targetDirectory.virtualFile
        val path = s.toNioPath()
        val targetPath = td.toNioPath().resolve(targetFileName)
        DdsManager.convertImageFormat(path, targetPath, "dds", "png")
        val t = findFile(targetPath, true)
        return t?.toPsiFile(file.project)
    }
}
