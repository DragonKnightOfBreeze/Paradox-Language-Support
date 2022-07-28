package icu.windea.pls.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*

class ParadoxScriptExpressionError(
	val description:String,
	val textRange: TextRange,
	val highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
)

fun ProblemsHolder.registerScriptExpressionError(element: PsiElement, error: ParadoxScriptExpressionError){
	registerProblem(element, error.description, error.highlightType, error.textRange)
}