package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.analysis.ParadoxGameTypeManager
import icu.windea.pls.model.analysis.ParadoxRootMetadata

/**
 * 游戏或模组信息。
 *
 * @property gameType 游戏类型。
 * @property rootFile 根目录。可以为空。
 *
 * @see ParadoxRootMetadata
 */
sealed interface ParadoxRootInfo {
    val rootFile: VirtualFile?
    val gameType: ParadoxGameType
    val gameVersion: String?
    val qualifiedName: String

    val steamId: String? get() = null
    val mainEntries: Set<String> get() = emptySet()
    val extraEntries: Set<String> get() = emptySet()

    sealed class MetadataBased(
        override val rootFile: VirtualFile,
        open val metadata: ParadoxRootMetadata,
    ) : ParadoxRootInfo {
        val name: String get() = metadata.name
        val version: String? get() = metadata.version
    }

    class Game(
        rootFile: VirtualFile,
        override val metadata: ParadoxRootMetadata.Game
    ) : MetadataBased(rootFile, metadata) {
        override val gameType: ParadoxGameType = ParadoxGameTypeManager.getGameType(this)
        override val gameVersion: String? = ParadoxGameTypeManager.getGameVersion(this)
        override val qualifiedName: String = ParadoxGameTypeManager.getGameQualifiedName(gameType, version)

        override val steamId: String get() = gameType.steamId
        override val mainEntries: Set<String> get() = gameType.metadata.gameMainEntries
        override val extraEntries: Set<String> get() = gameType.metadata.gameExtraEntries

        override fun toString() = qualifiedName
    }

    class Mod(
        rootFile: VirtualFile,
        override val metadata: ParadoxRootMetadata.Mod
    ) : MetadataBased(rootFile, metadata) {
        val gameTypeInfo: ParadoxGameTypeInfo? get() = metadata.gameTypeInfo
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture // 相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source

        val inferredGameType: ParadoxGameType? = gameTypeInfo?.gameType
        override val gameType: ParadoxGameType = ParadoxGameTypeManager.getGameType(this)
        override val gameVersion: String? = ParadoxGameTypeManager.getGameVersion(this)
        override val qualifiedName: String = ParadoxGameTypeManager.getModQualifiedName(gameType, name, version)

        override val steamId: String? get() = if (source == ParadoxModSource.Steam) remoteId else null
        override val mainEntries: Set<String> get() = gameType.metadata.modMainEntries
        override val extraEntries: Set<String> get() = gameType.metadata.modExtraEntries

        override fun toString() = qualifiedName
    }

    class Injected(
        override val rootFile: VirtualFile? = null,
        override val gameType: ParadoxGameType,
        override val gameVersion: String? = null,
    ) : ParadoxRootInfo {
        override val qualifiedName: String get() = PlsBundle.message("root.name.injected")
        override val steamId: String? get() = null

        override fun toString() = qualifiedName
    }
}
