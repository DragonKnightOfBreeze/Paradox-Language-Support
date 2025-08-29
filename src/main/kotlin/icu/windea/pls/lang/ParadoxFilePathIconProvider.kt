package icu.windea.pls.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.FilePathIconProvider
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.ParadoxFileType
import javax.swing.Icon

/**
 * 用于在VCS提交记录中直接基于文件路径为（可能已经不存在的）文件提供正确的图标。
 */
class ParadoxFilePathIconProvider : FilePathIconProvider {
    override fun getIcon(filePath: FilePath, project: Project?): Icon? {
        val possibleFileType = ParadoxFileType.resolvePossible(filePath.name)
        if (possibleFileType == ParadoxFileType.Other) return null

        val fileInfo = ParadoxCoreManager.getFileInfo(filePath) ?: return null
        val fileType = fileInfo.fileType
        return getIcon(fileType)
    }

    private fun getIcon(fileType: ParadoxFileType): Icon? {
        return when (fileType) {
            ParadoxFileType.Script -> PlsIcons.FileTypes.ParadoxScript
            ParadoxFileType.Localisation -> PlsIcons.FileTypes.ParadoxLocalisation
            ParadoxFileType.Csv -> PlsIcons.FileTypes.ParadoxCsv
            ParadoxFileType.ModDescriptor -> PlsIcons.FileTypes.ModeDescriptor
            else -> null
        }
    }
}
