package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

data class ParadoxFilePathIndexInfo(
    val directory: String,
    val included: Boolean,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
