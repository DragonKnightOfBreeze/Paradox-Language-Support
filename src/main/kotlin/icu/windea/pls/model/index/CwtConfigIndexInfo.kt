package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * 规则文件中的索引信息。
 *
 * @property gameType 游戏类型。
 *
 * @see icu.windea.pls.lang.index.CwtConfigIndexInfoAwareFileBasedIndex
 */
sealed class CwtConfigIndexInfo : IndexInfo() {
    abstract val gameType: ParadoxGameType
}
