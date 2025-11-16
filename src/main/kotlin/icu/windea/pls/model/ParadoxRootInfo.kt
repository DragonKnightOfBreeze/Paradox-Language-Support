package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import java.nio.file.Path

/**
 * 游戏或模组信息。
 *
 * @property gameType 游戏类型。
 */
sealed class ParadoxRootInfo {
    abstract val gameType: ParadoxGameType

    open val qualifiedName: String get() = PlsBundle.message("root.name.unnamed")
    open val steamId1: String? get() = null

    /**
     * @property rootFile 游戏或模组目录。
     * @property rootPath 游戏根目录。
     */
    sealed class MetadataBased(open val metadata: ParadoxMetadata) : ParadoxRootInfo() {
        override val gameType: ParadoxGameType get() = metadata.gameType

        val name: String get() = metadata.name
        val version: String? get() = metadata.version
        val rootFile: VirtualFile get() = metadata.rootFile
        val rootPath: Path get() = rootFile.toNioPath()
    }

    class Game(override val metadata: ParadoxMetadata.Game) : MetadataBased(metadata) {
        override val qualifiedName: String
            get() = buildString {
                append(gameType.title)
                if (version.isNotNullOrEmpty()) {
                    append("@").append(version)
                }
            }
        override val steamId1: String?
            get() = gameType.steamId
    }

    class Mod(override val metadata: ParadoxMetadata.Mod) : MetadataBased(metadata) {
        val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture // 相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source

        override val qualifiedName: String
            get() = buildString {
                append(gameType.title).append(" Mod: ")
                append(name.orNull() ?: PlsBundle.message("root.name.unnamed"))
                if (version.isNotNullOrEmpty()) {
                    append("@").append(version)
                }
            }
        override val steamId1: String?
            get() = if (metadata.source == ParadoxModSource.Steam) metadata.remoteId else null
    }

    class Injected(override val gameType: ParadoxGameType) : ParadoxRootInfo()
}
