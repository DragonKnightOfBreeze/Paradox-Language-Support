package icu.windea.pls.script.psi

import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost

/**
 * 参数。在不同类型的上下文中，存在不同的形式，且属于不同的节点角色。
 *
 * @see ParadoxScriptParameter
 * @see ParadoxScriptInlineMathParameter
 */
interface ParadoxParameter : NavigatablePsiElement, PsiPresentableElement, ParadoxScriptInterpolation, ParadoxLanguageInjectionHost {
    val idElement: PsiElement?

    override fun getName(): String?

    fun setName(name: String): ParadoxParameter

    val defaultValue: String? get() = null

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): ParadoxParameter {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxParameter> {
        return ParadoxScriptExpressionLiteralTextEscaper(this)
    }
}
