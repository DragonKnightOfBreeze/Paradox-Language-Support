package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxInferredScopeContextAwareDefinitionUsageInfo(
    val definitionName: String,
    val typeExpression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
