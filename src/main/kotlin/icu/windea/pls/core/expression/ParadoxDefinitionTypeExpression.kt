package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionTypeExpression.*

/**
 * 示例：
 * 
 * ```
 * civic_or_origin.civic
 * ```
 */
class ParadoxDefinitionTypeExpression(
	expressionString: String,
	val type: String,
	val subtypes: List<String>
): AbstractExpression(expressionString) {
	operator fun component1() = type
	operator fun component2() = subtypes
	
	companion object Resolver
}

fun Resolver.resolve(expression: String) : ParadoxDefinitionTypeExpression {
	val dotIndex = expression.indexOf('.')
	val type = if(dotIndex == -1) expression else expression.substring(0, dotIndex)
	val subtypes = if(dotIndex == -1) emptyList() else expression.substring(dotIndex + 1).split('.')
	return ParadoxDefinitionTypeExpression(expression, type, subtypes)
}