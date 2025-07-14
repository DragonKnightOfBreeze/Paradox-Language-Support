package icu.windea.pls.lang.psi

import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * @see ParadoxScriptExpressionElement
 * @see ParadoxLocalisationExpressionElement
 */
interface ParadoxExpressionElement : NavigatablePsiElement {
    override fun getName(): String

    val value: String

    fun setValue(value: String): ParadoxExpressionElement
}
