package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * 脚本文件和本地化文件中的索引信息。
 *
 * @property gameType 游戏类型。
 *
 * @see icu.windea.pls.lang.index.ParadoxIndexInfoAwareFileBasedIndex
 */
sealed class ParadoxIndexInfo : IndexInfo() {
    abstract val gameType: ParadoxGameType
}
