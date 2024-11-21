package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxOnActionInEventUsageInfo(
    val onActionName: String,
    val containingEventName: String,
    val containingEventScope: String?,
    val scopesElementOffset: Int,
    override val elementOffset: Int,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
