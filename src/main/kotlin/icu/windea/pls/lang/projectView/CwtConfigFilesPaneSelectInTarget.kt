package icu.windea.pls.lang.projectView

import com.intellij.ide.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.configGroup.*

class CwtConfigFilesPaneSelectInTarget(project: Project): ProjectViewSelectInTarget(project) {
    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        val vFile = PsiUtilCore.getVirtualFile(file)
        if(vFile == null || !vFile.isValid) return false
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(myProject) ?: return@f
            val relativePath = VfsUtil.getRelativePath(vFile, rootDirectory) ?: return@f
            if(relativePath.isNotNullOrEmpty()) return true
        }
        return false
    }
    
    override fun getMinorViewId() = CwtConfigFilesViewPane.ID
    
    override fun getWeight() = 200.0f //lower than ParadoxFilesPaneSelectInTarget
    
    override fun toString() = PlsBundle.message("select.in.cwt.config.files")
}
