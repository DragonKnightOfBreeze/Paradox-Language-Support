package icu.windea.pls.lang.psi

import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor
import icu.windea.pls.script.psi.ParadoxScriptExpressionLiteralTextEscaper

interface ParadoxLanguageInjectionHost : PsiLanguageInjectionHost, InjectionBackgroundSuppressor {
    // disable injection background highlight (by implementing InjectionBackgroundSuppressor)

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): ParadoxLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxLanguageInjectionHost> {
        return ParadoxScriptExpressionLiteralTextEscaper(this)
    }
}
