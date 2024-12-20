package icu.windea.pls.lang.psi

import com.intellij.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.script.psi.*

interface ParadoxScriptedVariableReference : PsiElement, NavigatablePsiElement {
    fun setName(name: String): ParadoxScriptedVariableReference

    override fun getReference(): ParadoxScriptedVariablePsiReference?

    val referenceValue: ParadoxScriptValue? get() = reference?.resolve()?.scriptedVariableValue
}
