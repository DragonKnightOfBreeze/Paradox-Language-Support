package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/***
 * 条件表达式。在不同类型的上下文中，存在不同的形式，且属于不同的节点角色。
 *
 * @see ParadoxScriptConditionalExpression
 */
interface ParadoxScriptConditionExpression: NavigatablePsiElement, PsiPresentableTextAwareElement
