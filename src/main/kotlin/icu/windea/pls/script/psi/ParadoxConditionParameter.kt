package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement

interface ParadoxConditionParameter : NavigatablePsiElement {
    override fun getName(): String?

    fun setName(name: String): ParadoxConditionParameter
}
