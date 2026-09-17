package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 可以作为表达式的 [PsiElement]。
 *
 * @see ParadoxCsvColumn
 */
interface ParadoxCsvExpressionElement : ParadoxExpressionElement {
    override fun getName(): String

    override val value: String get() = text

    override fun setValue(value: String): ParadoxCsvExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxCsvExpressionElement
}
