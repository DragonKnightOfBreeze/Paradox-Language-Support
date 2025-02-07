package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.nio.file.*

/**
 * 游戏或模组信息。
 * @property rootFile 游戏或模组目录。
 * @property entryFile 入口目录。注意入口目录不一定等同于游戏或模组目录。
 * @property gameType 游戏类型。
 * @property rootPath 游戏根目录。
 * @property entryPath 作为主要入口的根目录。
 */
sealed class ParadoxRootInfo(
    open val metadata: ParadoxMetadata
) {
    val name: String get() = metadata.name
    val version: String? get() = metadata.version
    val rootFile: VirtualFile get() = metadata.rootFile
    val entryFile: VirtualFile get() = metadata.entryFile
    val inferredGameType: ParadoxGameType? get() = metadata.inferredGameType
    val gameType: ParadoxGameType get() = metadata.gameType

    val rootPath: Path = rootFile.toNioPath()
    val entryPath: Path = entryFile.toNioPath()

    class Game(override val metadata: ParadoxMetadata.Game) : ParadoxRootInfo(metadata)

    class Mod(override val metadata: ParadoxMetadata.Mod) : ParadoxRootInfo(metadata) {
        val supportedVersion: String? get() = metadata.supportedVersion
        val picture: String? get() = metadata.picture //相对于模组目录的路径
        val tags: Set<String> get() = metadata.tags
        val remoteId: String? get() = metadata.remoteId
        val source: ParadoxModSource get() = metadata.source
    }
}

fun ParadoxMetadata.toRootInfo(): ParadoxRootInfo {
    return when (this) {
        is ParadoxMetadata.Game -> ParadoxRootInfo.Game(this)
        is ParadoxMetadata.Mod -> ParadoxRootInfo.Mod(this)
    }
}

val ParadoxRootInfo.qualifiedName: String
    get() = buildString {
        if (metadata is ParadoxMetadata.Game) {
            append(gameType.title)
        } else {
            append(gameType.title).append(" Mod: ")
            append(name.orNull() ?: PlsBundle.message("mod.name.unnamed"))
        }
        if (version.isNotNullOrEmpty()) {
            append("@").append(version)
        }
    }

val ParadoxRootInfo.steamId: String?
    get() = when (this) {
        is ParadoxRootInfo.Game -> gameType.steamId
        is ParadoxRootInfo.Mod -> if (metadata.source == ParadoxModSource.Steam) metadata.remoteId else null
    }
