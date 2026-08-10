package icu.windea.pls.model.index

import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.ep.index.ParadoxParameterMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxParameterLightElement
 * @see ParadoxMergedIndex
 * @see ParadoxParameterMergedIndexSupport
 */
data class ParadoxParameterIndexInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
