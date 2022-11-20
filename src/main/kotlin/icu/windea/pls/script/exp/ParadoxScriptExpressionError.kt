package icu.windea.pls.script.exp

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*

interface ParadoxScriptExpressionError {
	val rangeInExpression: TextRange
	val description: String
	val highlightType: ProblemHighlightType
}

fun ProblemsHolder.registerScriptExpressionError(element: PsiElement, error: ParadoxScriptExpressionError) {
	registerProblem(element, error.description, error.highlightType, error.rangeInExpression)
}
