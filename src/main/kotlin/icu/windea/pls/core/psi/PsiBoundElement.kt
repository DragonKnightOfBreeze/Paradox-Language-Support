package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

/**
 * 可能带有左右边界的 PSI 元素。左右边界都可能不存在。
 */
interface PsiBoundElement : PsiElement {
    val leftBound: PsiElement? get() = null
    val rightBound: PsiElement? get() = null
}
