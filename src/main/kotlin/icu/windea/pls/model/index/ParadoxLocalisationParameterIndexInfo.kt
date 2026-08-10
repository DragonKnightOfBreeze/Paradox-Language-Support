package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxLocalisationParameterMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxLocalisationParameterLightElement
 * @see ParadoxMergedIndex
 * @see ParadoxLocalisationParameterMergedIndexSupport
 */
data class ParadoxLocalisationParameterIndexInfo(
    val name: String,
    val localisationName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
