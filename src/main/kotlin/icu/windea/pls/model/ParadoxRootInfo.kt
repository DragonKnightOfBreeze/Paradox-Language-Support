package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.nio.file.*

/**
 * 游戏或模组信息。
 *
 * @property gameType 游戏类型。
 */
sealed class ParadoxRootInfo {
    abstract val gameType: ParadoxGameType

    /**
     * @property rootFile 游戏或模组目录。
     * @property entryFile 入口目录。注意入口目录不一定等同于游戏或模组目录。
     * @property rootPath 游戏根目录。
     * @property entryPath 作为主要入口的根目录。
     */
    sealed class MetadataBased(open val metadata: ParadoxMetadata) : ParadoxRootInfo() {
        override val gameType: ParadoxGameType get() = metadata.gameType

        val name: String get() = metadata.name
        val version: String? get() = metadata.version
        val rootFile: VirtualFile get() = metadata.rootFile
        val entryFile: VirtualFile get() = metadata.entryFile

       val rootPath: Path by lazy { rootFile.toNioPath() }
       val entryPath: Path by lazy { entryFile.toNioPath() }
    }

    class Game(override val metadata: ParadoxMetadata.Game) : MetadataBased(metadata)

    class Mod(override val metadata: ParadoxMetadata.Mod) : MetadataBased(metadata) {
        val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture //相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source
    }

    class Injected(override val gameType: ParadoxGameType) : ParadoxRootInfo()
}

fun ParadoxMetadata.toRootInfo(): ParadoxRootInfo {
    return when (this) {
        is ParadoxMetadata.Game -> ParadoxRootInfo.Game(this)
        is ParadoxMetadata.Mod -> ParadoxRootInfo.Mod(this)
    }
}

val ParadoxRootInfo.qualifiedName: String
    get() = when (this) {
        is ParadoxRootInfo.Game -> buildString {
            append(gameType.title)
            if (version.isNotNullOrEmpty()) {
                append("@").append(version)
            }
        }
        is ParadoxRootInfo.Mod -> buildString {
            append(gameType.title).append(" Mod: ")
            append(name.orNull() ?: PlsBundle.message("root.name.unnamed"))
            if (version.isNotNullOrEmpty()) {
                append("@").append(version)
            }
        }
        else -> PlsBundle.message("root.name.unnamed")
    }

val ParadoxRootInfo.steamId: String?
    get() = when (this) {
        is ParadoxRootInfo.Game -> gameType.steamId
        is ParadoxRootInfo.Mod -> if (metadata.source == ParadoxModSource.Steam) metadata.remoteId else null
        else -> null
    }
