package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxCsvColumn
 */
interface ParadoxCsvExpressionElement : ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxCsvExpressionElement

    override fun setContent(content: String, range: TextRange): ParadoxCsvExpressionElement
}
