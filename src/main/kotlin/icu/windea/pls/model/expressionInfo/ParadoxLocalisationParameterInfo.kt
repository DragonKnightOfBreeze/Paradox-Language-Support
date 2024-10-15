package icu.windea.pls.model.expressionInfo

import com.intellij.openapi.vfs.*
import icu.windea.pls.model.*

data class ParadoxLocalisationParameterInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile
    override var virtualFile: VirtualFile? = null
}
