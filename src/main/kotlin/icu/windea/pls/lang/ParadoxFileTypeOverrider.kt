package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.ep.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*

/**
 * 文件类型重载器。
 *
 * 基于文件后缀名以及相对于游戏或模组根目录的路径，将符合的文件重载为Paradox脚本文件或Paradox本地化文件。
 */
@Suppress("UnstableApiUsage")
class ParadoxFileTypeOverrider : FileTypeOverrider {
    //仅当从所在目录下找到launcher-settings.json或者descriptor.mod时
    //才有可能将所在目录（以及子目录）下的文件自动识别为Paradox本地化文件和脚本文件
    
    override fun getOverriddenFileType(file: VirtualFile): FileType? {
        //不需要重载无法获取内容的文件的文件类型
        if(file is VirtualFileWithoutContent) return null
        
        val fileInfo = ParadoxCoreHandler.getFileInfo(file) ?: return null
        return getFileType(fileInfo)
    }
    
    private fun getFileType(fileInfo: ParadoxFileInfo): LanguageFileType? {
        return when {
            //脚本文件
            fileInfo.fileType == ParadoxFileType.ParadoxScript -> ParadoxScriptFileType
            //本地化文件
            fileInfo.fileType == ParadoxFileType.ParadoxLocalisation -> ParadoxLocalisationFileType
            //目录或者其他文件
            else -> null
        }
    }
}

