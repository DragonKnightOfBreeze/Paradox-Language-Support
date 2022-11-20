package icu.windea.pls.script.exp.errors

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*

class ParadoxUnresolvedScopeFieldDataSourceExpressionError(
	override val rangeInExpression: TextRange,
	override val description: String
) : ParadoxScriptUnresolvedExpressionError
