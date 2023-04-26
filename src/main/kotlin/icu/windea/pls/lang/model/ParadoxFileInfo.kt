package icu.windea.pls.lang.model

import icons.*
import icu.windea.pls.*
import java.util.*
import javax.swing.*

/**
 * @property path 相对于游戏或模组根目录的路径。
 * @property entryPath 相对于入口目录（如，"game"）而非游戏或模组根目录的路径。
 */
class ParadoxFileInfo(
    val name: String,
    val path: ParadoxPath,
    val entryPath: ParadoxPath,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    //path - 用于显示在快速文档中，相对于游戏或模组根目录的路径
    //entryPath - 用于匹配CWT规则文件中指定的路径（后者一般以"game/"开始，插件会忽略掉此前缀），脚本文件中的路径引用**一般**基于这个
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo && path == other.path && entryPath == other.entryPath
            && fileType == other.fileType && rootInfo == other.rootInfo
    }
    
    override fun hashCode(): Int {
        return Objects.hash(path, entryPath, fileType, rootInfo)
    }
    
    fun getIcon(): Icon? {
        return when {
            fileType == ParadoxFileType.ParadoxScript && name.equals(PlsConstants.descriptorFileName, true) -> PlsIcons.DescriptorFile
            fileType == ParadoxFileType.ParadoxScript -> PlsIcons.ParadoxScriptFile
            fileType == ParadoxFileType.ParadoxLocalisation -> PlsIcons.ParadoxLocalisationFile
            else -> null
        }
    }
}
