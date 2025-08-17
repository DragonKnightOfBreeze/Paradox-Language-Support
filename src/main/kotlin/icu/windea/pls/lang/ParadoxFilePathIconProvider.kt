package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vcs.changes.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import javax.swing.*

/**
 * 用于在VCS提交记录中直接基于文件路径为（可能已经不存在的）文件提供正确的图标。
 */
class ParadoxFilePathIconProvider : FilePathIconProvider {
    override fun getIcon(filePath: FilePath, project: Project?): Icon? {
        if (!ParadoxFileManager.canBeParadoxFile(filePath)) return null
        val fileInfo = ParadoxCoreManager.getFileInfo(filePath) ?: return null
        return ParadoxFileManager.getFileIcon(fileInfo.fileType)
    }
}
