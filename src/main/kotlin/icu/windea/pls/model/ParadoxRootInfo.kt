package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.constants.DefaultStrings
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

    fun isValid(): Boolean

    fun invalidate()

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    sealed class MetadataBased(
        override val rootFile: VirtualFile,
        open val metadata: ParadoxRootMetadata,
    ) : ParadoxRootInfo {
        val name: String get() = metadata.name
        val version: String? get() = metadata.version

        @Volatile private var _isValid = true

        override fun isValid(): Boolean {
            return _isValid && rootFile.isValid
        }

        override fun invalidate() {
            _isValid = false
        }
    }

    data class Game(
        override val rootFile: VirtualFile,
        override val metadata: ParadoxRootMetadata.Game
    ) : MetadataBased(rootFile, metadata) {
        override val gameType: ParadoxGameType = ParadoxGameTypeManager.getGameType(this)
        override val gameVersion: String? = ParadoxGameTypeManager.getGameVersion(this)
        override val qualifiedName: String = ParadoxGameTypeManager.getGameQualifiedName(gameType, version)

        override val steamId: String get() = gameType.steamId
        override val mainEntries: Set<String> get() = gameType.metadata.gameMainEntries
        override val extraEntries: Set<String> get() = gameType.metadata.gameExtraEntries

        override fun toString() = "ParadoxRootInfo.Game(gameType=$gameType, gameVersion=$gameVersion)"
    }

    data class Mod(
        override val rootFile: VirtualFile,
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

        override fun toString() = "ParadoxRootInfo.Game(name=$name, version=$version, gameType=$gameType, gameVersion=$gameVersion)"
    }

    data class Injected(
        override val rootFile: VirtualFile? = null,
        override val gameType: ParadoxGameType,
        override val gameVersion: String? = null,
    ) : ParadoxRootInfo {
        override val qualifiedName: String get() = DefaultStrings.injected
        override val steamId: String? get() = null

        override fun isValid(): Boolean = true

        override fun invalidate() {}

        override fun equals(other: Any?) = super.equals(other) // use reference equality
        override fun hashCode() = super.hashCode() // use reference equality
        override fun toString() = "ParadoxRootInfo.Injected(gameType=$gameType, gameVersion=$gameVersion)"
    }
}
