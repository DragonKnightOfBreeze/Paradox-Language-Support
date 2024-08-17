package icu.windea.pls.lang.expression

import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.util.*

/**
 * 定义子类型表达式。
 *
 * 示例：
 *
 * * `a`
 * * `!a`
 * * `a&b`
 *
 * 用途：
 *
 * * 在CWT规则文件中，`subtype[X]`表示作为其值的子句中的规则仅限匹配此定义子类型表达式的定义。其中`X`即是一个定义子类型表达式。
 */
class ParadoxDefinitionSubtypeExpression private constructor(
    val expressionString: String
) {
    val subtypes: List<ReversibleValue<String>> = expressionString.split('&').map { ReversibleValue(it) }
    
    fun matches(subtypes: Collection<String>): Boolean {
        //目前仅支持"!"和"&"的组合
        return this.subtypes.all { t -> t.where { subtypes.contains(it) } }
    }
    
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.subtypes)
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionSubtypeExpression && expressionString == other.expressionString
    }
    
    override fun hashCode(): Int {
        return expressionString.hashCode()
    }
    
    override fun toString(): String {
        return expressionString
    }
    
    companion object Resolver {
        fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
            return ParadoxDefinitionSubtypeExpression(expressionString)
        }
    }
}
