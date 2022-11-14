package icu.windea.pls.script.expression

import icu.windea.pls.core.expression.*
import icu.windea.pls.script.exp.*

class ParadoxScriptSimpleExpression(
	expressionString: String,
	override val quoted: Boolean,
	override val type: ParadoxDataType,
	override val isKey: Boolean? = null
) : AbstractExpression(expressionString), ParadoxScriptExpression