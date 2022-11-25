package icu.windea.pls.script.exp.errors

import com.intellij.codeInspection.*

interface ParadoxMalformedExpressionError: ParadoxExpressionError {
	override val highlightType: ProblemHighlightType get() = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
}
