package icu.windea.pls.lang.resolve.expression.impl

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo

internal class ParadoxDefinitionSubtypeExpressionResolverImpl : ParadoxDefinitionSubtypeExpression.Resolver {
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
