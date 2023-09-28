package icu.windea.pls.model.expression

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxEventInOnActionInfo(
    val eventName: String,
    val typeExpression: String,
    val containingOnActionName: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}