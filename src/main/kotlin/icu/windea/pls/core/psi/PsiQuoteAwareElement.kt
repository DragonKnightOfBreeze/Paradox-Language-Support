package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.isQuoted

/**
 * 可以用引号括起的 PSI 元素。一般也可以不用引号括起。
 *
 * 备注：这里仅提供断言方法（如检查是否需要双引号），不直接提供操作方法（如用引号括起元素文本）。
 */
interface PsiQuoteAwareElement : PsiElement {
    val quoteChar: Char get() = '"'

    fun needQuote(): Boolean

    fun canQuote(): Boolean = !text.isQuoted(quoteChar)

    fun canUnquote(): Boolean = text.isQuoted(quoteChar) && !needQuote()
}
