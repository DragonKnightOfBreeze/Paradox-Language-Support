package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*

interface ParadoxConditionParameter : ParadoxTypedElement, NavigatablePsiElement {
    override fun getName(): String?

    fun setName(name: String): ParadoxConditionParameter

    override val type: ParadoxType get() = ParadoxType.Parameter
}
