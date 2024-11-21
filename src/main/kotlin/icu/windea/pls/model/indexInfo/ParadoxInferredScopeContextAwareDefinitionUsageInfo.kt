package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*

data class ParadoxInferredScopeContextAwareDefinitionUsageInfo(
    val definitionName: String,
    val typeExpression: String,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
