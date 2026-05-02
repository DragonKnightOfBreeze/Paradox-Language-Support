package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.analysis.ParadoxRootMetadata

/**
 * 游戏或模组信息。
 *
 * @property gameType 游戏类型。
 * @property rootFile 根目录。可以为空。
 *
 * @see ParadoxFileInfo
 */
sealed interface ParadoxRootInfo {
    val gameType: ParadoxGameType
    val rootFile: VirtualFile?

    val qualifiedName: String
    val steamId: String?

    val mainEntries: Set<String> get() = emptySet()
    val extraEntries: Set<String> get() = emptySet()

    sealed class MetadataBased(open val metadata: ParadoxRootMetadata) : ParadoxRootInfo {
        override val gameType: ParadoxGameType get() = metadata.gameType
        override val rootFile: VirtualFile get() = metadata.rootFile

        val name: String get() = metadata.name
        val version: String? get() = metadata.version
        val infoFile: VirtualFile? get() = metadata.infoFile
    }

    class Game(override val metadata: ParadoxRootMetadata.Game) : MetadataBased(metadata) {
        override val qualifiedName: String get() = metadata.qualifiedName
        override val steamId: String get() = metadata.steamId

        override val mainEntries: Set<String> get() = gameType.entryInfo.gameMain
        override val extraEntries: Set<String> get() = gameType.entryInfo.gameExtra

        override fun toString() = qualifiedName
    }

    class Mod(override val metadata: ParadoxRootMetadata.Mod) : MetadataBased(metadata) {
        val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture // 相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source

        override val qualifiedName: String get() = metadata.qualifiedName
        override val steamId: String? get() = metadata.steamId

        override val mainEntries: Set<String> get() = gameType.entryInfo.modMain
        override val extraEntries: Set<String> get() = gameType.entryInfo.modExtra

        override fun toString() = qualifiedName
    }

    class Injected(
        override val gameType: ParadoxGameType,
        override val rootFile: VirtualFile? = null,
    ) : ParadoxRootInfo {
        override val qualifiedName: String get() = PlsBundle.message("root.name.injected")
        override val steamId: String? get() = null

        override fun toString() = qualifiedName
    }
}
