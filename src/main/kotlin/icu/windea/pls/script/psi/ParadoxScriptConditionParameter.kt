package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement

/**
 * 条件参数。
 *
 * @see ParadoxScriptConditionalParameter
 */
interface ParadoxScriptConditionParameter : NavigatablePsiElement {
    val idElement: PsiElement?

    override fun getName(): String?

    fun setName(name: String): ParadoxScriptConditionParameter
}
