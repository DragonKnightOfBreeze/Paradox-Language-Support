package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxMeshLocatorMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxMeshLocatorLightElement
 * @see ParadoxMergedIndex
 * @see ParadoxMeshLocatorMergedIndexSupport
 */
data class ParadoxMeshLocatorIndexInfo(
    val name: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
