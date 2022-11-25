package icu.windea.pls.core.expression.errors

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.expression.*

interface ParadoxExpressionError {
	val rangeInExpression: TextRange
	val description: String
	val highlightType: ProblemHighlightType
}

fun ProblemsHolder.registerScriptExpressionError(element: PsiElement, error: ParadoxExpressionError) {
	registerProblem(element, error.description, error.highlightType, error.rangeInExpression )
}
