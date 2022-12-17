package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionTypeExpression.*

class ParadoxDefinitionTypeExpression(
	expressionString: String,
	val type: String,
	val subtype: String?
): AbstractExpression(expressionString) {
	operator fun component1() = type
	operator fun component2() = subtype
	
	companion object Resolver
}

fun Resolver.resolve(expression: String) : ParadoxDefinitionTypeExpression {
	val dotIndex = expression.indexOf('.')
	val type = if(dotIndex == -1) expression else expression.substring(0, dotIndex)
	val subtype = if(dotIndex == -1) null else expression.substring(dotIndex + 1)
	return ParadoxDefinitionTypeExpression(expression, type, subtype)
}