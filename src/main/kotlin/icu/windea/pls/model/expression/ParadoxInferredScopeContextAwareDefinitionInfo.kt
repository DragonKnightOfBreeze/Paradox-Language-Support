package icu.windea.pls.model.expression

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxInferredScopeContextAwareDefinitionInfo(
    val definitionName: String,
    val typeExpression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}