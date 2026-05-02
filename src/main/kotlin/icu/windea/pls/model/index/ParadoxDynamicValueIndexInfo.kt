package icu.windea.pls.model.index

import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxDynamicValueMergedIndexSupport
 */
data class ParadoxDynamicValueIndexInfo(
    val name: String,
    val dynamicValueType: String,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
