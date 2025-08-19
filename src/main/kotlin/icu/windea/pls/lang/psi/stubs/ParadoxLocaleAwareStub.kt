package icu.windea.pls.lang.psi.stubs

import com.intellij.psi.PsiElement

interface ParadoxLocaleAwareStub<T : PsiElement> : ParadoxStub<T> {
    val locale: String?
}
