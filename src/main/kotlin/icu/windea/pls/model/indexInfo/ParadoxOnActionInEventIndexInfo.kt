package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxOnActionInEventIndexInfo(
    val onActionName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
