package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxShaderEffectMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxShaderEffectLightElement
 * @see ParadoxMergedIndex
 * @see ParadoxShaderEffectMergedIndexSupport
 */
data class ParadoxShaderEffectIndexInfo(
    val name: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
