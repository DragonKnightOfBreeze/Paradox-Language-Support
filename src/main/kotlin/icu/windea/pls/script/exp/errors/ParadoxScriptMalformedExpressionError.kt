package icu.windea.pls.script.exp.errors

import com.intellij.codeInspection.*
import icu.windea.pls.script.exp.*

interface ParadoxScriptMalformedExpressionError: ParadoxScriptExpressionError {
	override val highlightType: ProblemHighlightType get() = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
}
