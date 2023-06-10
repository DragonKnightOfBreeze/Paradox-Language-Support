package icu.windea.pls.core.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxDefinitionSubtypeExpression.*
import icu.windea.pls.core.util.*
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
) : AbstractExpression(expressionString) {
    val subtypes: List<ReversibleValue<String>>
    
    init {
        subtypes = expressionString.split('&').map { ReversibleValue(it) }
    }
    
    fun matches(subtypes: Collection<String>): Boolean {
        //目前仅支持"!"和"&"的组合
        return this.subtypes.all { t -> t.where { subtypes.contains(it) } }
    }
    
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.subtypes)
    }
    
    companion object Resolver
}

fun Resolver.resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
    return ParadoxDefinitionSubtypeExpression(expressionString)
}
