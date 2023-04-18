package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionSubtypeExpression.*
import icu.windea.pls.lang.model.*

/**
 * 定义子类型表达式。
 * 
 * 示例：
 * 
 * ```
 * a
 * !a
 * a&b
 * !a&!b
 * ```
 * 
 * 用途：
 * 
 * * 在CWT文件中，`subtype[X]`表示作为其值的子句中的规则仅限匹配此定义子类型表达式的定义。其中`X`即是一个定义类型表达式。 
 */
class ParadoxDefinitionSubtypeExpression(
	expressionString: String
): AbstractExpression(expressionString) {
	companion object Resolver
	
	fun matches(subtypes: Collection<String>): Boolean {
		return true //TODO 0.9.10
	}
	
	fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
		return matches(definitionInfo.subtypes)
	}
}

fun Resolver.resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
	return ParadoxDefinitionSubtypeExpression(expressionString)
}
