package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement

interface ParadoxScriptedVariableReference : PsiElement, NavigatablePsiElement {
    fun setName(name: String): ParadoxScriptedVariableReference
}
