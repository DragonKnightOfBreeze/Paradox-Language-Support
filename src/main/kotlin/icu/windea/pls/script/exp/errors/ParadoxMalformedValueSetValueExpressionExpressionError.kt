package icu.windea.pls.script.exp.errors

import com.intellij.openapi.util.*

class ParadoxMalformedValueSetValueExpressionExpressionError(
	override val rangeInExpression: TextRange,
	override val description: String
) : ParadoxScriptMalformedExpressionError
