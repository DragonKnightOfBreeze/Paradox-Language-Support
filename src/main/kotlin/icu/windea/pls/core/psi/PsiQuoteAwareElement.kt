package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted

/**
 * 可以用引号括起的 PSI 元素。一般也可以不用引号括起。
 *
 * 备注：这里仅提供断言方法（如检查是否需要双引号），不直接提供操作方法（如用引号括起元素文本）。
 */
interface PsiQuoteAwareElement : PsiElement {
    val quoteChar: Char get() = '"'

    /** 根据输入的 [text]，检查是否需要首尾的引号，无论 [text] 是否已经用引号括起。 */
    fun needQuote(text: String): Boolean

    /** 根据输入的 [text]，检查是否可以添加周围的引号。 */
    fun canQuote(text: String): Boolean {
        return !text.isLeftQuoted(quoteChar) || !text.isRightQuoted(quoteChar)
    }

    /** 根据输入的 [text]，检查是否可以去除周围的引号。 */
    fun canUnquote(text: String): Boolean {
        return (text.isLeftQuoted(quoteChar) || text.isRightQuoted(quoteChar)) && !needQuote(text)
    }
}
