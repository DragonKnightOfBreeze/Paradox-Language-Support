package icu.windea.pls.model.expression.complex.errors

import com.intellij.codeInspection.*

interface ParadoxMissingExpressionError : ParadoxExpressionError {
    override val highlightType: ProblemHighlightType get() = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
}
