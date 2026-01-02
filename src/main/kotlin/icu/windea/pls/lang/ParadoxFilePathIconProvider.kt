package icu.windea.pls.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.FilePathIconProvider
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.model.ParadoxFileGroup
import javax.swing.Icon

/**
 * 用于在VCS提交记录中直接基于文件路径为（可能已经不存在的）文件提供正确的图标。
 */
@Suppress("removal", "OVERRIDE_DEPRECATION")
class ParadoxFilePathIconProvider : FilePathIconProvider {
    override fun getIcon(filePath: FilePath, project: Project?): Icon? {
        val possibleGroup = ParadoxFileGroup.resolvePossible(filePath.name)
        if (possibleGroup == ParadoxFileGroup.Other) return null

        val fileInfo = ParadoxAnalysisManager.getFileInfo(filePath) ?: return null
        return getIcon(fileInfo.group)
    }

    private fun getIcon(fileType: ParadoxFileGroup): Icon? {
        return when (fileType) {
            ParadoxFileGroup.Script -> PlsIcons.FileTypes.ParadoxScript
            ParadoxFileGroup.Localisation -> PlsIcons.FileTypes.ParadoxLocalisation
            ParadoxFileGroup.Csv -> PlsIcons.FileTypes.ParadoxCsv
            ParadoxFileGroup.ModDescriptor -> PlsIcons.FileTypes.ModDescriptor
            else -> null
        }
    }
}
