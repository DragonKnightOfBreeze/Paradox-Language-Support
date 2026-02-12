package icu.windea.pls.model.index

import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxOnActionInEventIndexInfoSupport
 */
data class ParadoxOnActionInEventIndexInfo(
    val onActionName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo() {
    val scopesElement: ParadoxScriptProperty?
        get() = file?.let { file -> ParadoxPsiFileManager.findPropertyFromStartOffset(file, scopesElementOffset) }
}
