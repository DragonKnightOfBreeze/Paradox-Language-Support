package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

data class ParadoxInlineScriptUsageInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}
