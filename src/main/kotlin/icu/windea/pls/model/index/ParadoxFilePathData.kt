package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * 文件路径数据。
 *
 * @property directory 文件所在目录。相对于入口目录，参见 [icu.windea.pls.model.ParadoxEntryInfo]。
 * @property included 是否被包含。被包含意味着会显示在文件视图中，并提供目录补全。
 * @property gameType 游戏类型。
 *
 * @see icu.windea.pls.lang.index.ParadoxFilePathIndex
 */
data class ParadoxFilePathData(
    val directory: String,
    val included: Boolean,
    val gameType: ParadoxGameType,
)
