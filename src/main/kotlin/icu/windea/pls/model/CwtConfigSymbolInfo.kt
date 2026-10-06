package icu.windea.pls.model

import icu.windea.pls.core.util.ReadWriteAccess

/**
 * 规则符号的解析信息。
 */
data class CwtConfigSymbolInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccess,
    val offset: Int,
    val elementOffset: Int,
    val gameType: ParadoxGameType
)
