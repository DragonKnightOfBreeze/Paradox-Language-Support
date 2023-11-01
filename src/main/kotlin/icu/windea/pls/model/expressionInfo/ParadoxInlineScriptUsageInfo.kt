package icu.windea.pls.model.expressionInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxInlineScriptUsageInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}
