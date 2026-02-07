package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

data class ParadoxEventInOnActionIndexInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
