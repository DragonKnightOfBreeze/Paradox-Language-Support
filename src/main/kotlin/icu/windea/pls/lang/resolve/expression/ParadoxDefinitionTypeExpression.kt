package icu.windea.pls.lang.resolve.expression

import icu.windea.pls.lang.resolve.expression.impl.ParadoxDefinitionTypeExpressionResolverImpl
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 定义类型表达式。
 *
 * 用途：
 * - 在查询定义时指定定义类型表达式，以进行过滤。
 * - 在规则文件中，数据表达式 `<X>` 用于匹配一个定义引用，其中 `X` 即是一个定义类型表达式。
 *
 * 示例：
 * ```
 * event
 * event.hidden
 * event.hidden.country_event
 * ```
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
