package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement

/**
 * 可以获取区别于原始文本的展示文本的 [PsiElement]。
 *
 * 说明：
 * - 这里获取的展示文本适用于各种描述文本和提示文本，而非导航和 UI 渲染。
 * - 如果可以用引号括起，通常需要直接保留。
 * - 对于字面量或普通文本，通常需要进行必要的截断。
 */
interface PsiPresentableTextAwareElement : PsiElement {
    val presentableText: String get() = text
}
