package icu.windea.pls.model.indexInfo

import com.intellij.openapi.vfs.*

data class ParadoxLocalisationParameterUsageInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
) : ParadoxIndexInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
