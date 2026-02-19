package icu.windea.pls.lang.resolve.expression

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

// region Implementations

private class ParadoxDefinitionTypeExpressionResolverImpl : ParadoxDefinitionTypeExpression.Resolver {
    override fun resolve(expressionString: String): ParadoxDefinitionTypeExpression {
        return ParadoxDefinitionTypeExpressionImpl(expressionString)
    }
}

private class ParadoxDefinitionTypeExpressionImpl(
    override val text: String
) : ParadoxDefinitionTypeExpression {
    override val type: String
    override val subtypes: List<String>

    init {
        val dotIndex = text.indexOf('.')
        type = if (dotIndex == -1) text else text.substring(0, dotIndex)
        subtypes = if (dotIndex == -1) emptyList() else text.substring(dotIndex + 1).split('.')
    }

    override fun matches(type: String, subtypes: Collection<String>): Boolean {
        return type == this.type && subtypes.containsAll(this.subtypes)
    }

    override fun matches(typeExpression: String): Boolean {
        return matches(ParadoxDefinitionTypeExpression.resolve(typeExpression))
    }

    override fun matches(typeExpression: ParadoxDefinitionTypeExpression): Boolean {
        return matches(typeExpression.type, typeExpression.subtypes)
    }

    override fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.type, definitionInfo.subtypes)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxDefinitionTypeExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
