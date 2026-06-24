package icu.windea.pls.script.psi

import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.lang.psi.ParadoxScriptExpressionLiteralTextEscaper

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, ParadoxScriptLiteralValue, ParadoxLanguageInjectionHost {
    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): ParadoxScriptStringExpressionElement {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxScriptStringExpressionElement> {
        return ParadoxScriptExpressionLiteralTextEscaper(this)
    }
}

