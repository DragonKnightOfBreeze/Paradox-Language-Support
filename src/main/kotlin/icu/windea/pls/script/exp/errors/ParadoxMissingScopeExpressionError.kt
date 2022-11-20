package icu.windea.pls.script.exp.errors

import com.intellij.openapi.util.*

class ParadoxMissingScopeExpressionError(
	override val rangeInExpression: TextRange,
	override val description: String
) : ParadoxScriptMissingExpressionError
