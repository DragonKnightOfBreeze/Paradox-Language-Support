package icu.windea.pls.model.index

import icu.windea.pls.model.ParadoxGameType

data class ParadoxEventInEventIndexInfo(
    val eventName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
