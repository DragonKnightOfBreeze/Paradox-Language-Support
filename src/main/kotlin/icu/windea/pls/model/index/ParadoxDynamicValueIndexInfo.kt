package icu.windea.pls.model.index

import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.ep.index.ParadoxDynamicValueMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxDynamicValueLightElement
 * @see ParadoxMergedIndex
 * @see ParadoxDynamicValueMergedIndexSupport
 */
data class ParadoxDynamicValueIndexInfo(
    val name: String,
    val type: String,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
