@file:Optimized

package icu.windea.pls.model.expressions

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.util.values.ReversibleValue

/**
 * 参数化块表达式。
 *
 * 其中的标识符为参数名，可以使用 `!` 取反。
 *
 * 用途：
 * - 在脚本文件中，`[[{x}]...]` 表示一个参数化块 ，其中 `{x}` 即是一个参数化块表达式。
 *
 * 示例：
 * ```text
 * PARAM
 * !PARAM
 * ```
 *
 * @see icu.windea.pls.script.psi.ParadoxScriptConditionalExpression
 */
interface ParadoxConditionalExpression {
    val text: String
    val part: ReversibleValue<String>

    fun matches(argumentNames: Collection<String>? = null): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolve(expressionString: String): ParadoxConditionalExpression
    }

    companion object {
        @JvmStatic
        fun resolve(expressionString: String): ParadoxConditionalExpression {
            return ParadoxConditionalExpressionResolver.resolve(expressionString)
        }
    }
}

// region Implementations

private object ParadoxConditionalExpressionResolver {
    fun resolve(expressionString: String): ParadoxConditionalExpression {
        return ParadoxConditionalExpressionImpl(expressionString)
    }
}

private class ParadoxConditionalExpressionImpl(
    override val text: String
) : ParadoxConditionalExpression {
    override val part: ReversibleValue<String> = ReversibleValue.from(text)

    override fun matches(argumentNames: Collection<String>?): Boolean {
        return part.withOperator { argumentNames != null && it in argumentNames }
    }

    override fun equals(other: Any?) = this === other || other is ParadoxConditionalExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
