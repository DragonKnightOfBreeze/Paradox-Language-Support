package icu.windea.pls.model

import icu.windea.pls.lang.index.ParadoxFilePathIndex

/**
 * 文件路径信息。存储与文件相关的补充信息。
 *
 * @property directory 文件所在目录。相对于入口目录，参见 [ParadoxEntryInfo]。
 * @property included 是否被包含。被包含意味着会显示在文件视图中，并提供目录补全。
 * @property gameType 对应的游戏类型。
 *
 * @see ParadoxFilePathIndex
 */
data class ParadoxFilePathInfo(
    val directory: String,
    val included: Boolean,
    val gameType: ParadoxGameType,
)
