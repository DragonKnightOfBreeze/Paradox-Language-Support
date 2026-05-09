package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path

/**
 * 文件信息。
 *
 * @property path 文件路径。相对于入口目录，参见 [icu.windea.pls.model.analysis.ParadoxGameTypeMetadata]。
 * @property entry 入口名称。参见 [icu.windea.pls.model.analysis.ParadoxGameTypeMetadata]。
 * @property group 文件分组。
 * @property rootInfo 游戏或模组信息。
 *
 * @see ParadoxRootInfo
 */
data class ParadoxFileInfo(
    val path: ParadoxPath,
    val entry: String,
    val group: ParadoxFileGroup,
    val rootInfo: ParadoxRootInfo,
) {
    val rootPath: Path? get() = rootInfo.rootFile?.toNioPath()
    val entryPath: Path? get() = if (entry.isEmpty()) rootPath else rootPath?.resolve(entry)

    val inMainOrExtraEntry: Boolean get() = inMainEntry || inExtraEntry
    val inMainEntry: Boolean get() = (entry.isEmpty() && rootInfo.mainEntries.isEmpty()) || entry in rootInfo.mainEntries
    val inExtraEntry: Boolean get() = entry in rootInfo.extraEntries

    /** 判断当前文件信息是否可能适用于 [file]（基于文件扩展名）。 */
    fun isPossible(file: VirtualFile): Boolean {
        return group == ParadoxFileGroup.resolvePossible(file.name)
    }

    /** 判断当前文件信息是否直接位于游戏或模组的根目录下（或者根目录本身）。 */
    fun isTopFromRoot(): Boolean {
        return path.length <= 1 && entry.isEmpty()
    }
}
