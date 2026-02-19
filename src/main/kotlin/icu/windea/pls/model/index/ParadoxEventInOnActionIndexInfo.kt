package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxEventInOnActionIndexInfoSupport
 */
data class ParadoxEventInOnActionIndexInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
