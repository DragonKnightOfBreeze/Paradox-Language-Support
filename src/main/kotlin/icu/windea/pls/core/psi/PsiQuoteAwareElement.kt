package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

/**
 * 可以用引号括起的 PSI 元素。一般来说也可以不用引号括起。
 *
 * 备注：这里仅提供用于获取 [QuotePattern] 的属性，不直接提供相关的断言和修改方法。推荐方式是使用 [String] 的各种相关的扩展函数。
 *
 * @see QuotePattern
 * @see QuotePatterns
 */
interface PsiQuoteAwareElement : PsiElement {
    val quotePattern: QuotePattern get() = QuotePatterns.Default
}
