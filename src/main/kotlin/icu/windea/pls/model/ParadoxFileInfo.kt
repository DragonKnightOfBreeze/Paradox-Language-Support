package icu.windea.pls.model

import icons.*
import icu.windea.pls.*
import java.util.*
import javax.swing.*

/**
 * @property path 相对于游戏或模组根目录的路径。
 * @property entryPath 作为入口的根目录相对于游戏根目录的路径。匹配CWT规则时，除了游戏根目录之外，也需要基于这些目录。
 * @property pathToEntry 相对于作为入口的根目录的路径。
 */
class ParadoxFileInfo(
    val name: String,
    val path: ParadoxPath,
    val entryPath: String?,
    val pathToEntry: ParadoxPath,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    //path - 用于显示在快速文档中，相对于游戏或模组根目录的路径
    //pathToEntry - 用于匹配CWT规则文件中指定的路径（后者一般以"game/"开始，插件会忽略掉此前缀），脚本文件中的路径引用**一般**基于这个
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo
            && path == other.path
            && entryPath == other.entryPath
            && fileType == other.fileType
            && rootInfo == other.rootInfo
    }
    
    override fun hashCode(): Int {
        return Objects.hash(path, entryPath, fileType, rootInfo)
    }
    
    fun isMainEntry(): Boolean {
        return entryPath == null || entryPath == "game"
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
