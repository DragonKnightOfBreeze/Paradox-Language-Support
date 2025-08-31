package icu.windea.pls.lang.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager

data class ParadoxComplexExpressionError(
    val code: Int,
    val rangeInExpression: TextRange,
    val description: String,
    val highlightType: ProblemHighlightType? = null
) {
    fun isUnresolvedError() = this.code in 1..100

    fun register(element: ParadoxExpressionElement, holder: ProblemsHolder, vararg fixes: LocalQuickFix) {
        val description = description
        val highlightType = when {
            highlightType != null -> highlightType
            isUnresolvedError() -> ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
            else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        }
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        holder.registerProblem(element, description, highlightType, rangeInElement, *fixes)
    }
}
