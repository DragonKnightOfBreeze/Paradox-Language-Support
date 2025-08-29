package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiListLikeElement

interface ParadoxScriptBlockElement : ParadoxScriptMemberContainer, PsiListLikeElement {
    val scriptedVariableList: List<ParadoxScriptScriptedVariable>

    override fun getComponents(): List<PsiElement>
}
