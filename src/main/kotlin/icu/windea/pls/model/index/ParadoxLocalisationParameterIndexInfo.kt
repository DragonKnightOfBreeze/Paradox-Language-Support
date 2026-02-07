package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

data class ParadoxLocalisationParameterIndexInfo(
    val name: String,
    val localisationName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
