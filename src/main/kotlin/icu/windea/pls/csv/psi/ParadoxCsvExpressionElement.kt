package icu.windea.pls.csv.psi

import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see ParadoxCsvColumn
 */
interface ParadoxCsvExpressionElement: ParadoxExpressionElement {
    override fun setValue(value: String): ParadoxCsvExpressionElement
}
