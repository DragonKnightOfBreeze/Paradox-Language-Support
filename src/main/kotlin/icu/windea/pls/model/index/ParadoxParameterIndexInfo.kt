package icu.windea.pls.model.index

import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxParameterMergedIndexSupport
 */
data class ParadoxParameterIndexInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
