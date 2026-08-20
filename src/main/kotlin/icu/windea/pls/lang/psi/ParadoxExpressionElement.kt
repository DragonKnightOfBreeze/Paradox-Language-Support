package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 作为表达式的 PSI 元素。
 *
 * 注意：如果更改了继承关系，需要对应地更改 [ParadoxPsiElementVisitor]。
 *
 * @see ParadoxExpression
 * @see ParadoxPsiElementVisitor
 * @see ParadoxScriptExpressionElement
 * @see ParadoxLocalisationExpressionElement
 * @see ParadoxCsvExpressionElement
 */
interface ParadoxExpressionElement : NavigatablePsiElement {
    override fun getName(): String

    val value: String

    fun setValue(value: String): ParadoxExpressionElement

    val expression: String
}
