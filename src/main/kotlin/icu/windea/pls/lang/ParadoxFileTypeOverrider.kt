package icu.windea.pls.lang

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*

/**
 * 文件类型重载器。
 *
 * 基于文件的扩展名以及相对于入口目录的路径，将符合的文件重载为脚本文件或本地化文件。
 */
class ParadoxFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        runCatchingCancelable r@{
            val fileInfoFromUserData = file.getUserData(PlsKeys.injectedFileInfo)
                ?: file.getUserData(PlsKeys.fileInfo)?.castOrNull<ParadoxFileInfo>()
                ?: return@r
            return doGetFileType(fileInfoFromUserData)
        }

        if (!ParadoxFilePathManager.canBeScriptOrLocalisationFile(file)) return null
        val fileInfo = ParadoxCoreManager.getFileInfo(file) ?: return null
        return doGetFileType(fileInfo)
    }

    private fun doGetFileType(fileInfo: ParadoxFileInfo): FileType? {
        return when (fileInfo.fileType) {
            ParadoxFileType.Script -> ParadoxScriptFileType
            ParadoxFileType.Localisation -> ParadoxLocalisationFileType
            ParadoxFileType.ModDescriptor -> ParadoxScriptFileType
            else -> null
        }
    }
}
