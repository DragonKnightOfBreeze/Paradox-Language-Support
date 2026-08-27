package icu.windea.pls.script.psi

import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, ParadoxScriptLiteralValue, ParadoxScriptInterpolationContainer, ParadoxLanguageInjectionHost, PsiQuoteAwareElement {
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
