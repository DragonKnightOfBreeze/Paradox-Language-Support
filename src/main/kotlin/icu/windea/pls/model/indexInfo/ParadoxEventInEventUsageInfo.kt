package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*

data class ParadoxEventInEventUsageInfo(
    val eventName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
