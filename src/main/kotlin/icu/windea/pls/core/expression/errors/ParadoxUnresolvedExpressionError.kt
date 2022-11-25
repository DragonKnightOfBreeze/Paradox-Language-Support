package icu.windea.pls.core.expression.errors

import com.intellij.codeInspection.*

interface ParadoxUnresolvedExpressionError: ParadoxExpressionError {
	override val highlightType: ProblemHighlightType get() = ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
}

