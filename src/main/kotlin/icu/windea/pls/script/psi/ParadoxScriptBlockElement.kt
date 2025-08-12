package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptBlockElement : ParadoxScriptMemberContainer, PsiListLikeElement {
    val scriptedVariableList: List<ParadoxScriptScriptedVariable>

    override fun getComponents(): List<PsiElement>
}
