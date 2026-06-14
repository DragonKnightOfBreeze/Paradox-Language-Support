package icu.windea.pls.model.expressions

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionExpression

/**
 * 参数条件表达式。
 *
 * 其中的标识符为参数名，可以使用 `!` 取反。
 *
 * 用途：
 * - 在脚本文件中，`[[{x}]...]` 表示一个参数条件块 ，其中 `{x}` 即是一个参数条件表达式。
 *
 * 示例：
 * ```
 * PARAM
 * !PARAM
 * ```
 *
 * @see ParadoxScriptParameterConditionExpression
 */
interface ParadoxParameterConditionExpression {
    val text: String
    val part: ReversibleValue<String>

    fun matches(argumentNames: Set<String>? = null): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(expressionString: String): ParadoxParameterConditionExpression
    }

    companion object {
        @JvmStatic
        fun resolve(expressionString: String): ParadoxParameterConditionExpression {
            return ParadoxParameterConditionExpressionResolver.resolve(expressionString)
        }
    }
}

// region Implementations

private object ParadoxParameterConditionExpressionResolver {
    fun resolve(expressionString: String): ParadoxParameterConditionExpression {
        return ParadoxParameterConditionExpressionImpl(expressionString)
    }
}

private class ParadoxParameterConditionExpressionImpl(
    override val text: String
) : ParadoxParameterConditionExpression {
    override val part: ReversibleValue<String> = ReversibleValue.from(text)

    override fun matches(argumentNames: Set<String>?): Boolean {
        return part.withOperator { argumentNames != null && it in argumentNames }
    }

    override fun equals(other: Any?) = this === other || other is ParadoxParameterConditionExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
