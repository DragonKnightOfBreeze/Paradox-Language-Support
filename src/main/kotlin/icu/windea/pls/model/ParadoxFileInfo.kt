package icu.windea.pls.model

import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.path.*
import icu.windea.pls.model.path.*
import java.util.*
import javax.swing.*

/**
 * @property path 相对于游戏或模组根目录的路径。
 * @property pathToEntry 相对于入口目录（如，"game"）而非游戏或模组根目录的路径。
 */
class ParadoxFileInfo(
    val name: String,
    val path: ParadoxPath,
    val entry: String?,
    val pathToEntry: ParadoxPath,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    //path - 用于显示在快速文档中，相对于游戏或模组根目录的路径
    //pathToEntry - 用于匹配CWT规则文件中指定的路径（后者一般以"game/"开始，插件会忽略掉此前缀），脚本文件中的路径引用**一般**基于这个
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo && path == other.path && pathToEntry == other.pathToEntry
            && fileType == other.fileType && rootInfo == other.rootInfo
    }
    
    override fun hashCode(): Int {
        return Objects.hash(path, pathToEntry, fileType, rootInfo)
    }
    
    fun isMainEntry(): Boolean {
        return entry == null || entry == "game"
    }
    
    fun getIcon(): Icon? {
        return when {
            fileType == ParadoxFileType.ParadoxScript && name.equals(PlsConstants.descriptorFileName, true) -> PlsIcons.FileTypes.ModeDescriptor
            fileType == ParadoxFileType.ParadoxScript -> PlsIcons.FileTypes.ParadoxScript
            fileType == ParadoxFileType.ParadoxLocalisation -> PlsIcons.FileTypes.ParadoxLocalisation
            else -> null
        }
    }
}
