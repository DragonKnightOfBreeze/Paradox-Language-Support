package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

interface ParadoxConditionParameter : NavigatablePsiElement {
    override fun getName(): String?

    fun setName(name: String): ParadoxConditionParameter
}
