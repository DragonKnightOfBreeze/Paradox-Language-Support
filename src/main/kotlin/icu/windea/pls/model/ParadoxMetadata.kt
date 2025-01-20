package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.ep.metadata.*

/**
 * 游戏或模组的元数据。用于获取游戏或模组的包括名称、版本、游戏类型在内的各种信息。
 *
 * 拥有多种来源。
 *
 * @see ParadoxMetadataProvider
 */
interface ParadoxMetadata {
    val forGame: Boolean
    val name: String
    val version: String?
    val inferredGameType: ParadoxGameType?
    val gameType: ParadoxGameType
    val rootFile: VirtualFile
    val entryFile: VirtualFile
}
