package icu.windea.pls.script.psi

import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.lang.psi.ParadoxScriptExpressionLiteralTextEscaper

interface ParadoxParameter : NavigatablePsiElement, ParadoxLanguageInjectionHost {
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
