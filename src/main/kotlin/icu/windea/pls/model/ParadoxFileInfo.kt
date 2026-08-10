package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.analysis.ParadoxGameTypeMetadata
import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path

/**
 * 文件信息。
 *
 * @property path 文件路径。相对于入口目录，参见 [ParadoxGameTypeMetadata]。
 * @property entry 入口名称。参见 [ParadoxGameTypeMetadata]。
 * @property group 文件分组。
 * @property rootInfo 游戏或模组信息。
 *
 * @see ParadoxRootInfo
 */
sealed interface ParadoxFileInfo {
    val path: ParadoxPath
    val entry: String
    val group: ParadoxFileGroup
    val rootInfo: ParadoxRootInfo

    val gameType: ParadoxGameType get() = rootInfo.gameType
    val gameVersion: String? get() = rootInfo.gameVersion

    val rootPath: Path? get() = rootInfo.rootFile?.toNioPath()
    val entryPath: Path? get() = if (entry.isEmpty()) rootPath else rootPath?.resolve(entry)

    /** 判断当前文件信息是否可能适用于 [file]（基于文件扩展名）。 */
    fun isPossible(file: VirtualFile): Boolean {
        return group == ParadoxFileGroup.resolvePossible(file.name)
    }

    /** 判断当前文件信息是否直接位于游戏或模组的根目录下（或者根目录本身）。 */
    fun isTopFromRoot(): Boolean {
        return path.length <= 1 && entry.isEmpty()
    }

    fun inMainEntry(): Boolean {
        return (entry.isEmpty() && rootInfo.mainEntries.isEmpty()) || entry in rootInfo.mainEntries
    }

    fun inExtraEntry(): Boolean {
        return entry in rootInfo.extraEntries
    }

    fun inMainOrExtraEntry(): Boolean {
        return inMainEntry() || inExtraEntry()
    }

    fun isValid(): Boolean

    fun invalidate()

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    data class Default(
        override val path: ParadoxPath,
        override val entry: String,
        override val group: ParadoxFileGroup,
        override val rootInfo: ParadoxRootInfo,
    ) : ParadoxFileInfo {
        @Volatile private var _isValid = true

        override fun isValid(): Boolean {
            return _isValid && rootInfo.isValid()
        }

        override fun invalidate() {
            _isValid = false
        }

        override fun toString() = "ParadoxRootInfo.Default(path=$path, entry=$entry, group=$group, rootInfo=$rootInfo)"
    }
}
