package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*

data class ParadoxInlineScriptUsageInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}
