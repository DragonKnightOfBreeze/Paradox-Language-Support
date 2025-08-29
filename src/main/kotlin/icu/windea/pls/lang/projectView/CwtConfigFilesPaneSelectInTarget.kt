package icu.windea.pls.lang.projectView

import com.intellij.ide.impl.ProjectViewSelectInTarget
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.PsiUtilCore
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider

class CwtConfigFilesPaneSelectInTarget(private val project: Project) : ProjectViewSelectInTarget(project) {
    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        val vFile = PsiUtilCore.getVirtualFile(file)
        if (vFile == null || !vFile.isValid) return false
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val relativePath = VfsUtil.getRelativePath(vFile, rootDirectory) ?: return@f
            if (relativePath.isNotNullOrEmpty()) return true
        }
        return false
    }

    override fun getMinorViewId() = CwtConfigFilesViewPane.ID

    override fun getWeight() = 200.0f //lower than ParadoxFilesPaneSelectInTarget

    override fun toString() = PlsBundle.message("select.in.cwt.config.files")
}
