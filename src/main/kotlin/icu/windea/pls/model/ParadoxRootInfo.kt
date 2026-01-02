package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull

/**
 * 游戏或模组信息。
 *
 * @property gameType 游戏类型。
 *
 * @see ParadoxRootMetadata
 */
sealed interface ParadoxRootInfo {
    val gameType: ParadoxGameType

    val mainEntries: Set<String> get() = emptySet()
    val extraEntries: Set<String> get() = emptySet()
    val qualifiedName: String get() = PlsBundle.message("root.name.unnamed")
    val steamId: String? get() = null

    sealed class MetadataBased(open val metadata: ParadoxRootMetadata) : ParadoxRootInfo {
        override val gameType: ParadoxGameType get() = metadata.gameType

        val name: String get() = metadata.name
        val version: String? get() = metadata.version
        val rootFile: VirtualFile get() = metadata.rootFile
        val infoFile: VirtualFile? get() = metadata.infoFile
    }

    class Game(override val metadata: ParadoxRootMetadata.Game) : MetadataBased(metadata) {
        override val mainEntries: Set<String>
            get() = gameType.entryInfo.gameMain
        override val extraEntries: Set<String>
            get() = gameType.entryInfo.gameExtra
        override val qualifiedName: String
            get() = buildString {
                append(gameType.title)
                if (version.isNotNullOrEmpty()) {
                    append("@").append(version)
                }
            }
        override val steamId: String
            get() = gameType.steamId
    }

    class Mod(override val metadata: ParadoxRootMetadata.Mod) : MetadataBased(metadata) {
        val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture // 相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source

        override val mainEntries: Set<String>
            get() = gameType.entryInfo.modMain
        override val extraEntries: Set<String>
            get() = gameType.entryInfo.modExtra
        override val qualifiedName: String
            get() = buildString {
                append(gameType.title).append(" Mod: ")
                append(name.orNull() ?: PlsBundle.message("root.name.unnamed"))
                if (version.isNotNullOrEmpty()) {
                    append("@").append(version)
                }
            }
        override val steamId: String?
            get() = if (metadata.source == ParadoxModSource.Steam) metadata.remoteId else null
    }

    class Injected(override val gameType: ParadoxGameType) : ParadoxRootInfo
}
