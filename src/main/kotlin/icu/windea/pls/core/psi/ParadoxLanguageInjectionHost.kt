package icu.windea.pls.core.psi

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*

interface ParadoxLanguageInjectionHost : PsiLanguageInjectionHost, InjectionBackgroundSuppressor {
    //disable injection background highlight (by implementing InjectionBackgroundSuppressor)
}