package icu.windea.pls.core.model

import com.intellij.psi.*
import icu.windea.pls.lang.model.*

class ParadoxLocalisationParameterInfo(
    val name: String,
    val localisationName: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
): ParadoxExpressionInfo {
    @Volatile override var file: PsiFile? = null //unused yet
}