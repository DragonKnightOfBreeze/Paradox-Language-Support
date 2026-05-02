package icu.windea.pls.model.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.analysis.ParadoxAnalysisUtil
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource

/**
 * 游戏或模组的元数据。
 *
 * 用于检测游戏目录模组目录，以及获取包括名称、版本、游戏类型在内的各种信息。
 *
 * 基于类别（游戏/模组）以及游戏类型（[icu.windea.pls.model.ParadoxGameType]），可以有多种不同的来源。
 *
 *
 * @see ParadoxMetadataInfo
 * @see icu.windea.pls.ep.analysis.ParadoxRootMetadataProvider
 */
sealed interface ParadoxRootMetadata {
    val name: String
    val version: String?
    val gameType: ParadoxGameType
    val rootFile: VirtualFile
    val infoFile: VirtualFile?

    val qualifiedName: String
    val steamId: String?

    interface Game : ParadoxRootMetadata {
        override val qualifiedName: String get() = ParadoxAnalysisUtil.getGameQualifiedName(gameType, version)
        override val steamId: String get() = gameType.steamId
    }

    interface Mod : ParadoxRootMetadata {
        val inferredGameType: ParadoxGameType?
        val supportedVersion: String?
        val picture: String? // 相对于模组目录的路径
        val tags: Set<String>
        val remoteId: String?
        val source: ParadoxModSource
        val presentablePath: String?

        override val qualifiedName: String get() = ParadoxAnalysisUtil.getModQualifiedName(gameType, name, version)
        override val steamId: String? get() = if (source == ParadoxModSource.Steam) remoteId else null

        fun isValid(): Boolean = true
    }
}
