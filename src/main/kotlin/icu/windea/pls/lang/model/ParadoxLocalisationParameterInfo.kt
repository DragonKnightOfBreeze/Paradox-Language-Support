package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*

data class ParadoxLocalisationParameterInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
): ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
}