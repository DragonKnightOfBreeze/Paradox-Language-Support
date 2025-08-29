package icu.windea.pls.lang.projectView

import com.intellij.ide.impl.ProjectViewSelectInTarget
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.PsiUtilCore
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo

class ParadoxFilesPaneSelectInTarget(project: Project) : ProjectViewSelectInTarget(project) {
    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        val vFile = PsiUtilCore.getVirtualFile(file)
        if (vFile == null || !vFile.isValid) return false
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return false
        if (fileInfo.path.length == 1 && vFile.isFile) return false //排除直接位于根目录下的文件
        return true
    }

    override fun getMinorViewId() = ParadoxFilesViewPane.ID

    override fun getWeight() = 100.0f //very low

    override fun toString() = PlsBundle.message("select.in.paradox.files")
}
