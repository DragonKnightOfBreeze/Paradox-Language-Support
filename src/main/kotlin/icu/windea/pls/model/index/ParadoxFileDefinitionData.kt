package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * 文件定义数据。
 *
 * @see icu.windea.pls.lang.index.ParadoxFileDefinitionIndex
 */
data class ParadoxFileDefinitionData(
    val name: String,
    val type: String,
    val subtypes: List<String>?,
    val typeKey: String,
    val gameType: ParadoxGameType,
)
