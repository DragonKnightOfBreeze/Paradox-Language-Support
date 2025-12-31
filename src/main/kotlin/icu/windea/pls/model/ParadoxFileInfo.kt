package icu.windea.pls.model

import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path
import java.util.*

/**
 * 文件信息。
 *
 * @property path 文件路径。相对于入口目录，参见 [ParadoxEntryInfo]。
 * @property entry 入口名称。参见 [ParadoxEntryInfo]。
 * @property group 文件分组。
 * @property rootInfo 游戏或模组信息。
 */
class ParadoxFileInfo(
    val path: ParadoxPath,
    val entry: String,
    val group: ParadoxFileGroup,
    val rootInfo: ParadoxRootInfo,
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
            return if (entry.isEmpty()) rootPath else rootPath.resolve(entry)
        }

    val inMainOrExtraEntry: Boolean get() = inMainEntry || inExtraEntry
    val inMainEntry: Boolean get() = entry.isEmpty() && rootInfo.mainEntries.isEmpty() || entry in rootInfo.mainEntries
    val inExtraEntry: Boolean get() = entry in rootInfo.extraEntries

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxFileInfo
            && path == other.path && entry == other.entry
            && group == other.group && rootInfo == other.rootInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(path, entry, group, rootInfo)
    }
}
