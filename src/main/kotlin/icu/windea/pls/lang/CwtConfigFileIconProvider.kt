package icu.windea.pls.lang

import com.intellij.ide.FileIconProvider
import com.intellij.ide.projectView.impl.ProjectRootsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import javax.swing.Icon

/**
 * 为规则目录以及其中的规则文件提供特殊的图标。
 */
class CwtConfigFileIconProvider : FileIconProvider, DumbAware {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if (project == null) return null
        if (file.isDirectory) {
            if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
            if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
            val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
            val fileProvider = fileProviders.find { it.getRootDirectory(project) == file }
            if (fileProvider == null) return null
            val icon = PlsIcons.General.ConfigGroupDirectory
            return icon
        } else {
            // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
            if (file.fileType != CwtFile) return null
            val configGroup = CwtConfigManager.getContainingConfigGroup(file, project)
            if (configGroup == null) return null
            val icon = PlsIcons.FileTypes.CwtConfig
            return icon
        }
    }
}
