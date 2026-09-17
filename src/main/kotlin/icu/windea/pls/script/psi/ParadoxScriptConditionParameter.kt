package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 条件参数。在不同类型的上下文中，存在不同的形式，且属于不同的节点角色。
 *
 * @see ParadoxScriptConditionalParameter
 */
interface ParadoxScriptConditionParameter : NavigatablePsiElement, PsiPresentableTextAwareElement {
    val idElement: PsiElement?

    override fun getName(): String?

    fun setName(name: String): ParadoxScriptConditionParameter
}
