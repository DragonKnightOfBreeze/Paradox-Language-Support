package icu.windea.pls.script.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 传入参数。
 *
 * @see ParadoxScriptNormalParameterArgument
 * @see ParadoxScriptInlineMathParameterArgument
 */
interface ParadoxScriptArgument : NavigatablePsiElement, PsiPresentableTextAwareElement {
    val value: String

    fun setValue(value: String): ParadoxScriptArgument

    fun setContent(content: String, range: TextRange): ParadoxScriptArgument
}
