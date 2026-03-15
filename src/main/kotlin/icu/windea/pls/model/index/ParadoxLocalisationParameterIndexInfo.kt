package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxLocalisationParameterMergedIndexSupport
 */
data class ParadoxLocalisationParameterIndexInfo(
    val name: String,
    val localisationName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
