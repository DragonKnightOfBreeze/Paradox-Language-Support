package icu.windea.pls.model.expressions

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 定义子类型表达式。
 *
 * 其中的标识符为定义的子类型，可以使用 `!` 取反，使用 `&` 取交集。
 *
 * 用途：
 * - 在规则文件中，声明规则中的 `subtype[{x}] = {...}` 表示此子句中的规则仅适用于子类型匹配 `{x}` 的定义，其中 `{x}` 即是一个定义子类型表达式。
 *
 * 示例：
 * - `a`
 * - `!b`
 * - `a&!b`
 */
interface ParadoxDefinitionSubtypeExpression {
    val text: String
    val parts: List<ReversibleValue<String>>

    fun matches(subtypes: Collection<String>): Boolean
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
            return ParadoxDefinitionSubtypeExpressionResolver.resolve(expressionString)
        }
    }
}

// region Implementations

private object ParadoxDefinitionSubtypeExpressionResolver {
    fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
        return ParadoxDefinitionSubtypeExpressionImpl(expressionString)
    }
}

private class ParadoxDefinitionSubtypeExpressionImpl(
    override val text: String
) : ParadoxDefinitionSubtypeExpression {
    override val parts: List<ReversibleValue<String>> = text.split('&').map { ReversibleValue.from(it) }

    override fun matches(subtypes: Collection<String>): Boolean {
        // 目前仅支持"!"和"&"的组合
        return this.parts.all { t -> t.withOperator { subtypes.contains(it) } }
    }

    override fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.subtypes)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxDefinitionSubtypeExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
