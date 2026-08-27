package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

interface PsiPresentableElement : PsiElement {
    /**
     * 非语义的展示文本。对于字面量或者文本，通常需要进行必要的截断。如果可以用引号括起，需要直接保留。
     */
    val presentableText: String get() = text
}
