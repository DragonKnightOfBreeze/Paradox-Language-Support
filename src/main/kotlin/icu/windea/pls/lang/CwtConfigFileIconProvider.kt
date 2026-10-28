package icu.windea.pls.lang

import com.intellij.ide.FileIconProvider
import com.intellij.ide.projectView.impl.ProjectRootsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.configGroup.CwtConfigFileService
import icu.windea.pls.cwt.CwtFileType
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
            val isRootDirectory = CwtConfigFileService.getInstance().isRootDirectory(file, project)
            if (isRootDirectory) {
                return ChronicleIcons.General.ConfigGroupDirectory
            }
        } else {
            // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
            if (file.fileType !== CwtFileType) return null
            val configGroup = CwtConfigFileService.getInstance().getContainingConfigGroup(file, project)
            if (configGroup != null) {
                return ChronicleIcons.FileTypes.CwtConfig
            }
        }
        return null
    }
}
