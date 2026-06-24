package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

interface PsiBoundElement: PsiElement {
    val leftBound: PsiElement? get() = null
    val rightBound: PsiElement? get() = null
}
