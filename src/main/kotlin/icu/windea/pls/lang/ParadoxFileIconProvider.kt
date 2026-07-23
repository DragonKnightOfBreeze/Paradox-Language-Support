package icu.windea.pls.lang

import com.intellij.ide.FileIconProvider
import com.intellij.ide.projectView.impl.ProjectRootsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.model.ParadoxFileGroup
import javax.swing.Icon

/**
 * 为游戏或模组目录、入口目录以及模组描述符文件提供特殊图标。
 */
class ParadoxFileIconProvider : FileIconProvider, DumbAware {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if (file.isDirectory) {
            if (project != null) {
                if (ProjectRootsUtil.isModuleContentRoot(file, project)) return null
                if (ProjectRootsUtil.isModuleSourceRoot(file, project)) return null
            }
            val fileInfo = ParadoxAnalysisManager.getFileInfo(file) ?: return null
            val rootInfo = fileInfo.rootInfo
            if (rootInfo.rootFile != null && file == rootInfo.rootFile) {
                return ChronicleIcons.General.RootDirectory(rootInfo)
            }
            if (fileInfo.path.isEmpty()) {
                return ChronicleIcons.General.EntryDirectory
            }
        } else {
            val group = ParadoxFileGroup.resolvePossible(file.name)
            if (group == ParadoxFileGroup.ModDescriptor) {
                return ChronicleIcons.FileTypes.ModDescriptor
            }
        }
        return null
    }
}

