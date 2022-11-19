package icu.windea.pls.script.exp

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*

class ParadoxScriptExpressionError(
	val rangeInExpression: TextRange,
	val description: String,
	val highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
)

fun ProblemsHolder.registerScriptExpressionError(element: PsiElement, error: icu.windea.pls.script.expression.ParadoxScriptExpressionError) {
	registerProblem(element, error.description, error.highlightType, error.textRange)
}
