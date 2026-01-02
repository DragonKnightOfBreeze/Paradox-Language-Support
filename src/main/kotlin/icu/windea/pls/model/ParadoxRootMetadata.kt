package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile

/**
 * 游戏或模组的元数据。
 *
 * 用于检测游戏目录与模组目录，以及获取包括名称、版本、游戏类型在内的各种信息。
 * 基于类别（游戏/模组）以及游戏类型（[ParadoxGameType]），可以有多种不同的来源。
 *
 * @see ParadoxRootInfo
 */
sealed interface ParadoxRootMetadata {
    val name: String
    val version: String?
    val gameType: ParadoxGameType
    val rootFile: VirtualFile
    val infoFile: VirtualFile?

    interface Game : ParadoxRootMetadata

    interface Mod : ParadoxRootMetadata {
        val inferredGameType: ParadoxGameType?
        val supportedVersion: String?
        val picture: String? // 相对于模组目录的路径
        val tags: Set<String>
        val remoteId: String?
        val source: ParadoxModSource
    }
}
