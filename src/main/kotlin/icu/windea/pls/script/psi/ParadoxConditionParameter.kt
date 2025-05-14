package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.lang.psi.ParadoxTypedElement
import icu.windea.pls.model.ParadoxType

interface ParadoxConditionParameter : ParadoxTypedElement, NavigatablePsiElement {
    override fun getName(): String?

    fun setName(name: String): ParadoxConditionParameter

    override val type: ParadoxType get() = ParadoxType.Parameter
}
