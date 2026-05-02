package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import icu.windea.pls.model.analysis.ParadoxRootMetadataInfo
import java.nio.file.Path

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
    val qualifiedName: String
    val steamId: String?

    val mainEntries: Set<String> get() = emptySet()
    val extraEntries: Set<String> get() = emptySet()

    sealed class MetadataBased(
        override val rootFile: VirtualFile,
        open val metadata: ParadoxRootMetadata,
    ) : ParadoxRootInfo {
        override val gameType: ParadoxGameType get() = metadata.gameType
        val name: String get() = metadata.name
        val version: String? get() = metadata.version
        val rootPath: Path get() = metadata.rootPath
        val infoPath: Path? get() = metadata.infoPath
        val info: ParadoxRootMetadataInfo? get() = metadata.info
        val infoPresentablePath: String? get() = metadata.infoPresentablePath
    }

    class Game(
        rootFile: VirtualFile,
        override val metadata: ParadoxRootMetadata.Game
    ) : MetadataBased(rootFile, metadata) {
        override val qualifiedName: String get() = metadata.qualifiedName
        override val steamId: String get() = metadata.steamId

        override val mainEntries: Set<String> get() = gameType.metadata.gameMainEntries
        override val extraEntries: Set<String> get() = gameType.metadata.gameExtraEntries

        override fun toString() = qualifiedName
    }

    class Mod(
        rootFile: VirtualFile,
        override val metadata: ParadoxRootMetadata.Mod
    ) : MetadataBased(rootFile, metadata) {
        val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture // 相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source

        override val qualifiedName: String get() = metadata.qualifiedName
        override val steamId: String? get() = metadata.steamId

        override val mainEntries: Set<String> get() = gameType.metadata.modMainEntries
        override val extraEntries: Set<String> get() = gameType.metadata.modExtraEntries

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
