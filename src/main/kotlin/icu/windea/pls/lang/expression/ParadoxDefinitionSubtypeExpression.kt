package icu.windea.pls.lang.expression

import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.expression.impl.ParadoxDefinitionSubtypeExpressionResolverImpl
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 定义子类型表达式。
 *
 * 示例：
 * - `a`
 * - `!a`
 * - `a&b`
 *
 * 用途：
 * - 在CWT规则文件中，`subtype[X]`表示作为其值的子句中的规则仅限匹配此定义子类型表达式的定义。其中`X`即是一个定义子类型表达式。
 */
interface ParadoxDefinitionSubtypeExpression {
    val text: String
    val subtypes: List<ReversibleValue<String>>

    fun matches(subtypes: Collection<String>): Boolean
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression
    }

    companion object : Resolver by ParadoxDefinitionSubtypeExpressionResolverImpl()
}
