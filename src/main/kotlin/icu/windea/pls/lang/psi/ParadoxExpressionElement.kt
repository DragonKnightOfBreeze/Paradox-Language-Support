package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * @see ParadoxScriptExpressionElement
 * @see ParadoxLocalisationExpressionElement
 * @see ParadoxCsvExpressionElement
 */
interface ParadoxExpressionElement : NavigatablePsiElement {
    override fun getName(): String

    val value: String

    fun setValue(value: String): ParadoxExpressionElement
}
