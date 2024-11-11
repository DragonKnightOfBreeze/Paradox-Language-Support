package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxEventInEventUsageInfo(
    val eventName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
