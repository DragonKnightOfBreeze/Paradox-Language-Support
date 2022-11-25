package icu.windea.pls.core.expression.errors

import com.intellij.codeInspection.*

interface ParadoxMissingExpressionError: ParadoxExpressionError {
	override val highlightType: ProblemHighlightType get() = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
}
