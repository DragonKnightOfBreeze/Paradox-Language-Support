package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 作为片段的 [com.intellij.psi.PsiElement]。
 *
 * 对应的文本实际上并非完整的（定义、本地化等目标的）引用文本，而是其去除特定的前后缀之后的形式。
 */
interface ParadoxSnippetElement: NavigatablePsiElement {
    // TODO 3.0.4
}
