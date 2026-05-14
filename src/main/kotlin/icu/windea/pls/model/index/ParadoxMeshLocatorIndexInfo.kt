package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxMeshLocatorMergedIndexSupport
 */
data class ParadoxMeshLocatorIndexInfo(
    val name: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
