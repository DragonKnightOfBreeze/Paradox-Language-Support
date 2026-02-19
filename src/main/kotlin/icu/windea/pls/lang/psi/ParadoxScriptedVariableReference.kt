package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement

interface ParadoxScriptedVariableReference : NavigatablePsiElement {
    fun setName(name: String): ParadoxScriptedVariableReference
}
