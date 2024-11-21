package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*

data class ParadoxEventInOnActionUsageInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
