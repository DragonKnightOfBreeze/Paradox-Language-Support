package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 条件参数。
 *
 * @see ParadoxScriptConditionalBlockParameter
 */
interface ParadoxConditionParameter : NavigatablePsiElement {
    override fun getName(): String?

    fun setName(name: String): ParadoxConditionParameter
}
