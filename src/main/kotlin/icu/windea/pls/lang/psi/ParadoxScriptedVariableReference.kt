package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference

interface ParadoxScriptedVariableReference : PsiElement, NavigatablePsiElement {
    fun setName(name: String): ParadoxScriptedVariableReference

    fun resolved() = reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}
