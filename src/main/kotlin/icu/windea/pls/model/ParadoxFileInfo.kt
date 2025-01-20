package icu.windea.pls.model

import java.util.*

/**
 * 文件信息。
 * @property path 匹配规则时使用的路径。相对于入口目录。
 * @property entryName 入口名称。即入口目录相对于游戏或模组目录的路径。
 * @property fileType 检测得到的文件类型。
 * @property rootInfo 游戏或模组的根信息。
 */
class ParadoxFileInfo(
    val path: ParadoxPath,
    val entryName: String,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo
            && path == other.path && entryName == other.entryName
            && fileType == other.fileType && rootInfo == other.rootInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(path, entryName, fileType, rootInfo)
    }
}

fun ParadoxFileInfo.inMainEntry(): Boolean {
    return entryName.isEmpty() || entryName == "game"
}
