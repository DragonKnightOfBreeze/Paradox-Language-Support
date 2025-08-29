package icu.windea.pls.lang.psi

import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor

interface ParadoxLanguageInjectionHost : PsiLanguageInjectionHost, InjectionBackgroundSuppressor {
    //disable injection background highlight (by implementing InjectionBackgroundSuppressor)
}
