package icu.windea.pls.model.usageInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxLocalisationParameterUsageInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
) : ParadoxUsageInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
