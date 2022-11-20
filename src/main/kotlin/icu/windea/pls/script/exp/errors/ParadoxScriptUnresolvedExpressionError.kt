package icu.windea.pls.script.exp.errors

import com.intellij.codeInspection.*
import icu.windea.pls.script.exp.*

interface ParadoxScriptUnresolvedExpressionError: ParadoxScriptExpressionError {
	override val highlightType: ProblemHighlightType get() = ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
}

