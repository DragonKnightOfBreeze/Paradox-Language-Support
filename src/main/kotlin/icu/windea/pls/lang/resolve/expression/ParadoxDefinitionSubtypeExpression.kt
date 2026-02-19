package icu.windea.pls.lang.resolve.expression

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.ParadoxDefinitionInfo

/**
 * 定义子类型表达式。
 *
 * 用途：
 * - 在规则文件中，`subtype[X]` 表示作为其值的子句中的规则仅限匹配此定义子类型表达式的定义。其中 `X` 即是一个定义子类型表达式。
 *
 * 示例：
 * ```
 * a
 * !b
 * a&!b
 * ```
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

// region Implementations

private class ParadoxDefinitionSubtypeExpressionResolverImpl : ParadoxDefinitionSubtypeExpression.Resolver {
    override fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
        return ParadoxDefinitionSubtypeExpressionImpl(expressionString)
    }
}

private class ParadoxDefinitionSubtypeExpressionImpl(
    override val text: String
) : ParadoxDefinitionSubtypeExpression {
    override val subtypes: List<ReversibleValue<String>> = text.split('&').map { ReversibleValue.from(it) }

    override fun matches(subtypes: Collection<String>): Boolean {
        // 目前仅支持"!"和"&"的组合
        return this.subtypes.all { t -> t.withOperator { subtypes.contains(it) } }
    }

    override fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.subtypes)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxDefinitionSubtypeExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
