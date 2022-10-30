package icu.windea.pls.script.expression

import icu.windea.pls.core.expression.*

class ParadoxScriptSimpleExpression(
	expressionString: String,
	override val quoted: Boolean,
	override val type: ParadoxScriptExpressionType,
	override val isKey: Boolean? = null
) : AbstractExpression(expressionString), ParadoxScriptExpression