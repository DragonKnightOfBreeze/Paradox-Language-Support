package icu.windea.pls.lang.projectView

import com.intellij.ide.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.configGroup.*

class CwtConfigFilesPaneSelectInTarget(private val project: Project): ProjectViewSelectInTarget(project) {
    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        val vFile = PsiUtilCore.getVirtualFile(file)
        if(vFile == null || !vFile.isValid) return false
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEachFast f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val relativePath = VfsUtil.getRelativePath(vFile, rootDirectory) ?: return@f
            if(relativePath.isNotNullOrEmpty()) return true
        }
        return false
    }
    
    override fun getMinorViewId() = CwtConfigFilesViewPane.ID
    
    override fun getWeight() = 200.0f //lower than ParadoxFilesPaneSelectInTarget
    
    override fun toString() = PlsBundle.message("select.in.cwt.config.files")
}
