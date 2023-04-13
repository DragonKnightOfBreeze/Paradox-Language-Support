package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionTypeExpression.*
import icu.windea.pls.lang.model.*

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
	
	fun matches(type: String, subtypes: List<String>): Boolean {
		return type == this.type && subtypes.containsAll(this.subtypes)
	}
	
	fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
		return definitionInfo.type == this.type && definitionInfo.subtypes.containsAll(this.subtypes)
	}
	
	companion object Resolver
}

fun Resolver.resolve(expression: String) : ParadoxDefinitionTypeExpression {
	val dotIndex = expression.indexOf('.')
	val type = if(dotIndex == -1) expression else expression.substring(0, dotIndex)
	val subtypes = if(dotIndex == -1) emptyList() else expression.substring(dotIndex + 1).split('.')
	return ParadoxDefinitionTypeExpression(expression, type, subtypes)
}