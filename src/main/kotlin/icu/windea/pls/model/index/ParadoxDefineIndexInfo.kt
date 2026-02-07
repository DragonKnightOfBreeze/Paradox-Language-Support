package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

data class ParadoxDefineIndexInfo(
    val namespace: String,
    val variable: String?,
    val elementOffsets: Set<Int>,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
