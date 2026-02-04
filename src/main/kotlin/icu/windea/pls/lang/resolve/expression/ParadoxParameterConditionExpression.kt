package icu.windea.pls.lang.resolve.expression

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.lang.resolve.expression.impl.ParadoxParameterConditionExpressionResolverImpl

/**
 * 参数条件表达式。
 *
 * 用途：
 * - 在脚本文件中，`[[X]...]` 表示一个参数条件块 ，其中 `X` 即是一个参数条件表达式。
 *
 * 示例：
 * ```
 * PARAM
 * !PARAM
 * ```
 */
interface ParadoxParameterConditionExpression {
    val text: String
    val snippet: ReversibleValue<String>

    fun matches(argumentNames: Set<String>? = null): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(expressionString: String): ParadoxParameterConditionExpression
    }

    companion object : Resolver by ParadoxParameterConditionExpressionResolverImpl()
}
