package icu.windea.pls.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithoutContent
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxFileGroup

/**
 * 文件类型重载器。
 *
 * 基于文件的扩展名以及相对于入口目录的路径，将符合的文件重载为脚本文件或本地化文件。
 */
class ParadoxFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        if (file.isDirectory) return null
        if (file is VirtualFileWithoutContent) return null

        if (file is VirtualFileWindow) {
            val fileInfo = ParadoxAnalysisManager.getFileInfo(file) ?: return null
            return ParadoxFileManager.getFileType(fileInfo.group)
        }

        val possibleGroup = ParadoxFileGroup.resolvePossible(file.name)
        if (possibleGroup == ParadoxFileGroup.Other) return null

        if (ParadoxAnalysisInjector.useDefaultFileExtensions()) {
            val fileType = ParadoxFileManager.getFileType(possibleGroup)
            if (fileType != null) return fileType
        }

        val fileInfo = ParadoxAnalysisManager.getFileInfo(file) ?: return null
        return ParadoxFileManager.getFileType(fileInfo.group)
    }
}
