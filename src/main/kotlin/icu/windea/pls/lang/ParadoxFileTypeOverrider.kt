package icu.windea.pls.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithoutContent
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.analyze.ParadoxAnalyzeManager
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

        runCatchingCancelable r@{
            val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfo) ?: return@r
            return ParadoxFileManager.getFileType(injectedFileInfo.group)
        }

        runCatchingCancelable r@{
            val fileInfo = file.getUserData(PlsKeys.cachedFileInfo)?.value ?: return@r
            return ParadoxFileManager.getFileType(fileInfo.group)
        }

        if (file is VirtualFileWindow) {
            val fileInfo = ParadoxAnalyzeManager.getFileInfo(file) ?: return null
            return ParadoxFileManager.getFileType(fileInfo.group)
        }

        val possibleGroup = ParadoxFileGroup.resolvePossible(file.name)
        if (possibleGroup == ParadoxFileGroup.Other) return null

        if (ParadoxFileManager.isTestDataFile(file)) {
            return ParadoxFileManager.getFileType(possibleGroup)
        }

        val fileInfo = ParadoxAnalyzeManager.getFileInfo(file) ?: return null
        return ParadoxFileManager.getFileType(fileInfo.group)
    }
}
