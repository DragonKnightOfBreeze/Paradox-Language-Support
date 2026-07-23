package icu.windea.pls.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.FilePathIconProvider
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.model.ParadoxFileGroup
import javax.swing.Icon

/**
 * 在 VCS 提交记录中直接基于文件路径为文件提供特殊图标。这些文件可能并不存在于本地.
 */
class ParadoxFilePathIconProvider : FilePathIconProvider {
    override fun getIcon(filePath: FilePath, isDirectory: Boolean, project: Project?): Icon? {
        if (isDirectory) {
            val fileInfo = ParadoxAnalysisManager.getFileInfo(filePath) ?: return null
            val rootInfo = fileInfo.rootInfo
            if (rootInfo.rootFile != null && filePath.virtualFile == rootInfo.rootFile) {
                return ChronicleIcons.General.RootDirectory(rootInfo)
            }
            if (fileInfo.path.isEmpty()) {
                return ChronicleIcons.General.EntryDirectory
            }
        } else {
            val group = ParadoxFileGroup.resolvePossible(filePath.name)
            if (group == ParadoxFileGroup.ModDescriptor) {
                return ChronicleIcons.FileTypes.ModDescriptor
            }
        }
        return null
    }
}
