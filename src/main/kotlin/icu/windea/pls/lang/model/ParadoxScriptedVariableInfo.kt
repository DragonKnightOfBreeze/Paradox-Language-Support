package icu.windea.pls.lang.model

import com.intellij.psi.*

data class ParadoxScriptedVariableInfo(
    val name: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
): ParadoxElementInfo {
    @Volatile override var file: PsiFile? = null
}