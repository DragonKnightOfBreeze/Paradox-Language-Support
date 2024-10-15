package icu.windea.pls.lang.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

interface ParadoxParameter : ParadoxTypedElement, NavigatablePsiElement, ParadoxLanguageInjectionHost {
    override fun getName(): String?

    fun setName(name: String): ParadoxParameter

    val defaultValue: String? get() = null

    override val type: ParadoxType get() = ParadoxType.Parameter

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

