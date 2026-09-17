package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 参数。在不同类型的上下文中，存在不同的形式，且属于不同的节点角色。
 *
 * 说明：
 * - 参数语法可能携带默认值（default value），可能存在对应的传入参数（argument）。
 * - 参数语法在不同类型的上下文中，在细节上可能存在不同之处，并且可能存在不同的形式。
 * - 实际上，脚本文件中的任何地方都能使用参数语法，这意味着无法提供完美的语法支持。
 *
 * @see ParadoxScriptNormalParameter
 * @see ParadoxScriptInlineMathParameter
 */
interface ParadoxScriptParameter : ParadoxScriptInterpolation, NavigatablePsiElement, PsiPresentableTextAwareElement  {
    val idElement: PsiElement?

    override fun getName(): String?

    fun setName(name: String): ParadoxScriptParameter

    val defaultValue: String? get() = null
}
