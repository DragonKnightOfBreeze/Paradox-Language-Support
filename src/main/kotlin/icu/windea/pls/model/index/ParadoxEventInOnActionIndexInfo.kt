package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxEventInOnActionMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxMergedIndex
 * @see ParadoxEventInOnActionMergedIndexSupport
 */
data class ParadoxEventInOnActionIndexInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
