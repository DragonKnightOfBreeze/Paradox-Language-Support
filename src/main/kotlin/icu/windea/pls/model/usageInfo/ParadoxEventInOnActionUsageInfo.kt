package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxEventInOnActionUsageInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val elementOffset: Int,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
