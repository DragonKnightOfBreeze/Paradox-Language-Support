package icu.windea.pls.script.exp.errors

import com.intellij.openapi.util.*

class ParadoxMissingScopeFieldExpressionExpressionError(
	override val rangeInExpression: TextRange,
	override val description: String
) : ParadoxMissingExpressionError
