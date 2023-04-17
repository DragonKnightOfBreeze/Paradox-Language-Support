package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionTypeExpression.*
import icu.windea.pls.lang.model.*

/**
 * 定义类型表达式。
 * 
 * 示例：
 * 
 * ```
 * event
 * event.hidden
 * event.hidden.country_event
 * ```
 * 
 * 用途：
 * 
 * * 查询定义指定定义类型表达式，以进行过滤。
 * * 在CWT文件中，`<X>`表示一个定义引用，其中`X`即是一个定义类型表达式。
 */
class ParadoxDefinitionTypeExpression(
	expressionString: String,
	val type: String,
	val subtypes: List<String>
): AbstractExpression(expressionString) {
	operator fun component1() = type
	operator fun component2() = subtypes
	
	fun matches(type: String, subtypes: Collection<String>): Boolean {
		return type == this.type && subtypes.containsAll(this.subtypes)
	}
	
	fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
		return matches(definitionInfo.type, definitionInfo.subtypes)
	}
	
	companion object Resolver
}

fun Resolver.resolve(expression: String) : ParadoxDefinitionTypeExpression {
	val dotIndex = expression.indexOf('.')
	val type = if(dotIndex == -1) expression else expression.substring(0, dotIndex)
	val subtypes = if(dotIndex == -1) emptyList() else expression.substring(dotIndex + 1).split('.')
	return ParadoxDefinitionTypeExpression(expression, type, subtypes)
}

