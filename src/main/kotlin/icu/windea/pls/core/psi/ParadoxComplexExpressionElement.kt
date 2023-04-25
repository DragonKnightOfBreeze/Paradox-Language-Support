package icu.windea.pls.core.psi

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*

interface ParadoxComplexExpressionElement: NavigatablePsiElement, PsiLanguageInjectionHost, InjectionBackgroundSuppressor {
    //PsiLanguageInjectionHost - 允许进行语言注入
    //InjectionBackgroundSuppressor - 不提供背景高亮
    
    override fun isValidHost(): Boolean {
        return true
    }
    
    override fun updateText(text: String): PsiLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text)
    }
    
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return StringLiteralEscaper(this)
    }
}