package icu.windea.pls.model

import icu.windea.pls.model.paths.ParadoxPath
import java.util.*

/**
 * 文件信息。
 * @property path 文件路径。相对于入口目录，参见 [ParadoxGameType.EntryNames]。
 * @property entryName 入口名称。参见 [ParadoxGameType.EntryNames]。
 * @property fileType 检测得到的文件类型。
 * @property rootInfo 游戏或模组的根信息。
 */
class ParadoxFileInfo(
    val path: ParadoxPath,
    val entryName: String,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    fun inMainEntry(): Boolean {
        return entryName.isEmpty() || entryName == "game"
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo
            && path == other.path && entryName == other.entryName
            && fileType == other.fileType && rootInfo == other.rootInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(path, entryName, fileType, rootInfo)
    }
}
