package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiPresentableElement

/**
 * 条件化块。在不同类型的上下文中，存在不同的形式，且属于不同的节点角色。
 *
 * @see ParadoxScriptNormalConditionalBlock
 * @see ParadoxScriptInlineConditionalBlock
 */
interface ParadoxScriptConditionalBlock : NavigatablePsiElement, PsiPresentableElement, PsiBoundElement {
    val conditionalExpression: ParadoxScriptConditionalExpression?
}

// TODO 3.0.2+ introduce `ParadoxScriptInlineMathConditionalBlock`
