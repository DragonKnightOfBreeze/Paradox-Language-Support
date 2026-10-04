package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

/**
 * 作为外部引用的 [PsiElement]。
 *
 * 对应的声明处实际上位于（区别于常规的脚本文件、本地化文件、CSV 文件的）外部文件中。
 */
interface ParadoxExternalReferenceElement : NavigatablePsiElement, PsiNameIdentifierOwner {
    override fun getName(): String
}
