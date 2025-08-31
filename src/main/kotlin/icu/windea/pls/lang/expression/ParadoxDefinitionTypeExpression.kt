package icu.windea.pls.lang.expression

import icu.windea.pls.lang.expression.impl.ParadoxDefinitionTypeExpressionResolverImpl
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 定义类型表达式。
 *
 * 示例：
 * - `event`
 * - `event.hidden`
 * - `event.hidden.country_event`
 *
 * 用途：
 * - 查询定义时指定定义类型表达式，以进行过滤。
 * - 在CWT规则文件中，`<X>`表示一个定义引用，其中`X`即是一个定义类型表达式。
 */
interface ParadoxDefinitionTypeExpression {
    val text: String
    val type: String
    val subtypes: List<String>

    operator fun component1() = type
    operator fun component2() = subtypes

    fun matches(type: String, subtypes: Collection<String>): Boolean
    fun matches(typeExpression: ParadoxDefinitionTypeExpression): Boolean
    fun matches(typeExpression: String): Boolean
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(expressionString: String): ParadoxDefinitionTypeExpression
    }

    companion object : Resolver by ParadoxDefinitionTypeExpressionResolverImpl()
}
