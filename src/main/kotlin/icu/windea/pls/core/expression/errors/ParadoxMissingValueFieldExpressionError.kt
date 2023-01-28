package icu.windea.pls.core.expression.errors

import com.intellij.openapi.util.*

class ParadoxMissingValueFieldExpressionError(
	override val rangeInExpression: TextRange,
	override val description: String
) : ParadoxMissingExpressionError

