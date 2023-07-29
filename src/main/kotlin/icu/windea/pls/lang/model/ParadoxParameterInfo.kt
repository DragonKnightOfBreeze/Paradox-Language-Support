package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.vfs.*

data class ParadoxParameterInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}
