package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.ide.projectView.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.ep.configGroup.*
import javax.swing.*

/**
 * 为规则目录以及其中的规则文件提供特殊的图标。
 */
class CwtConfigFileIconProvider : FileIconProvider, DumbAware {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if (project == null) return null
        if (file.isDirectory) {
            if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
            if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
            val fileProviders = CwtConfigGroupFileProvider.INSTANCE.EP_NAME.extensionList
            val fileProvider = fileProviders.find { it.getRootDirectory(project) == file }
            if (fileProvider == null) return null
            val icon = PlsIcons.General.ConfigGroupDirectory
            return icon
        } else {
            val fileProviders = CwtConfigGroupFileProvider.INSTANCE.EP_NAME.extensionList
            val fileProvider = fileProviders.find { it.getContainingConfigGroup(file, project) != null }
            if (fileProvider == null) return null
            val icon = PlsIcons.FileTypes.CwtConfig
            return icon
        }
    }
}
