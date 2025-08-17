package icu.windea.pls.lang

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.*

/**
 * 文件类型重载器。
 *
 * 基于文件的扩展名以及相对于入口目录的路径，将符合的文件重载为脚本文件或本地化文件。
 */
class ParadoxFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        if(file.isDirectory) return null

        runCatchingCancelable r@{
            val injectedFileInfo = file.getUserData(PlsKeys.injectedFileInfo) ?: return@r
            return ParadoxFileManager.getFileType(injectedFileInfo.fileType)
        }

        runCatchingCancelable r@{
            val fileInfo = file.getUserData(PlsKeys.fileInfo)?.castOrNull<ParadoxFileInfo>() ?: return@r
            return ParadoxFileManager.getFileType(fileInfo.fileType)
        }

        if (!ParadoxFileManager.canBeParadoxFile(file)) return null
        val fileInfo = ParadoxCoreManager.getFileInfo(file) ?: return null
        return ParadoxFileManager.getFileType(fileInfo.fileType)
    }
}
