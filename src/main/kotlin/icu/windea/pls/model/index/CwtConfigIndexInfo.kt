package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * 规则文件中的索引信息。
 *
 * @property gameType 对应的游戏类型。
 */
interface CwtConfigIndexInfo : IndexInfo {
    val gameType: ParadoxGameType
}
