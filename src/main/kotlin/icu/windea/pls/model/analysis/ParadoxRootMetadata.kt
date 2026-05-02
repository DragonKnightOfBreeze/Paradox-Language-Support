package icu.windea.pls.model.analysis

import icu.windea.pls.ep.analysis.ParadoxRootMetadataProvider
import icu.windea.pls.lang.analysis.ParadoxAnalysisUtil
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

/**
 * 游戏或模组的元数据。
 *
 * 用于检测游戏目录模组目录，以及获取包括名称、版本、游戏类型在内的各种信息。
 *
 * 基于类别（游戏/模组）以及游戏类型（[icu.windea.pls.model.ParadoxGameType]），可以有多种不同的来源。
 *
 * @property gameType 游戏类型。
 * @property rootFile 游戏或模组的根目录。
 * @property name 名字。
 * @property version 版本。可以为空。
 * @property info 元数据信息。可以为空。
 * @property infoPath 元数据的路径。可以为空。
 * @property infoPresentablePath 用于显示的元数据的路径。相对于根目录，并使用 `/` 作为路径分隔符。
 *
 * @see ParadoxRootInfo
 * @see ParadoxRootMetadataInfo
 * @see ParadoxRootMetadataProvider
 */
sealed interface ParadoxRootMetadata {
    val gameType: ParadoxGameType
    val name: String
    val version: String?
    val rootPath: Path
    val infoPath: Path?
    val info: ParadoxRootMetadataInfo?
    val infoPresentablePath: String?

    val qualifiedName: String
    val steamId: String?

    interface Game : ParadoxRootMetadata {
        override val qualifiedName: String get() = ParadoxAnalysisUtil.getGameQualifiedName(gameType, version)
        override val steamId: String get() = gameType.steamId
    }

    interface Mod : ParadoxRootMetadata {
        val inferredGameType: ParadoxGameType?
        val supportedVersion: String?
        val picture: String? // 相对于模组根目录的路径
        val tags: Set<String>
        val remoteId: String?
        val source: ParadoxModSource

        override val qualifiedName: String get() = ParadoxAnalysisUtil.getModQualifiedName(gameType, name, version)
        override val steamId: String? get() = if (source == ParadoxModSource.Steam) remoteId else null
    }
}
