package icu.windea.pls.model

import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path
import java.util.*

/**
 * 文件信息。
 *
 * @property path 文件路径。相对于入口目录，参见 [ParadoxEntryInfo]。
 * @property entryName 入口名称。参见 [ParadoxEntryInfo]。
 * @property fileType 检测得到的文件类型。
 * @property rootInfo 游戏或模组的根信息。
 */
class ParadoxFileInfo(
    val path: ParadoxPath,
    val entryName: String,
    val fileType: ParadoxFileType,
    val rootInfo: ParadoxRootInfo
) {
    val rootPath: Path?
        get() {
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
            return rootInfo.rootFile.toNioPath()
        }
    val entryPath: Path?
        get() {
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val rootPath = rootInfo.rootFile.toNioPath()
            return if (entryName.isEmpty()) rootPath else rootPath.resolve(entryName)
        }

    /**
     * 是否位于主要入口目录中。参见 [ParadoxEntryInfo]。
     */
    fun inMainEntries(): Boolean {
        val entryInfo = rootInfo.gameType.entryInfo
        val mainEntries = when (rootInfo) {
            is ParadoxRootInfo.Game -> entryInfo.gameMain
            is ParadoxRootInfo.Mod -> entryInfo.modMain
            else -> emptySet()
        }
        return mainEntries.isEmpty() || entryName in mainEntries
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
