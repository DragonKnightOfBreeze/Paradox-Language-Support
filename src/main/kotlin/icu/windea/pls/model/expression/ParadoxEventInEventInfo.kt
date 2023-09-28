package icu.windea.pls.model.expression

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxEventInEventInfo(
    val eventName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}