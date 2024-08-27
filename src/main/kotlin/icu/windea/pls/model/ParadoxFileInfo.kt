package icu.windea.pls.model

import icons.*
import icu.windea.pls.*
import java.util.*
import javax.swing.*

/**
 * @property path 相对于入口目录的路径。用于匹配规则。
 * @property relPath 相对于游戏或模组目录的路径。用于定位文件。
 */
class ParadoxFileInfo(
    val path: ParadoxPath,
    val relPath: ParadoxPath,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo 
            && relPath == other.relPath && fileType == other.fileType && rootInfo == other.rootInfo
    }
    
    override fun hashCode(): Int {
        return Objects.hash(relPath, fileType, rootInfo)
    }
    
    fun inMainEntry(): Boolean {
        return relPath == path || relPath.root == "game"
    }
    
    fun getIcon(): Icon? {
        return when {
            fileType == ParadoxFileType.ParadoxScript && relPath.fileName.equals(PlsConstants.descriptorFileName, true) -> PlsIcons.FileTypes.ModeDescriptor
            fileType == ParadoxFileType.ParadoxScript -> PlsIcons.FileTypes.ParadoxScript
            fileType == ParadoxFileType.ParadoxLocalisation -> PlsIcons.FileTypes.ParadoxLocalisation
            else -> null
        }
    }
}
