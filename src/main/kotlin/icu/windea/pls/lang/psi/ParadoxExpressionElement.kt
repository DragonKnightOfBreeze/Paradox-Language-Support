package icu.windea.pls.lang.psi

import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.csv.psi.*

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
