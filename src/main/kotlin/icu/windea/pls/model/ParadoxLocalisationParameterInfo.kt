package icu.windea.pls.model

import com.intellij.openapi.vfs.*

data class ParadoxLocalisationParameterInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
): ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}