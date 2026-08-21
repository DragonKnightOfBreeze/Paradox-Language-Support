package icu.windea.pls.model.index

import icu.windea.pls.ep.index.ParadoxEventInEventMergedIndexSupport
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiService

/**
 * @see ParadoxMergedIndex
 * @see ParadoxEventInEventMergedIndexSupport
 */
data class ParadoxEventInEventIndexInfo(
    val eventName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val scopesElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxScriptPsiService.findPropertyFromStartOffset(file, scopesElementOffset) }
}
