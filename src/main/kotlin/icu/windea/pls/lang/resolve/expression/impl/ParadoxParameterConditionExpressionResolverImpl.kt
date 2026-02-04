package icu.windea.pls.lang.resolve.expression.impl

import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.lang.resolve.expression.ParadoxParameterConditionExpression

internal class ParadoxParameterConditionExpressionResolverImpl : ParadoxParameterConditionExpression.Resolver {
    override fun resolve(expressionString: String): ParadoxParameterConditionExpression {
        return ParadoxParameterConditionExpressionImpl(expressionString)
    }
}

private class ParadoxParameterConditionExpressionImpl(
    override val text: String
) : ParadoxParameterConditionExpression {
    override val snippet: ReversibleValue<String> = ReversibleValue.from(text)

    override fun matches(argumentNames: Set<String>?): Boolean {
        return snippet.withOperator { argumentNames != null && it in argumentNames }
    }

    override fun equals(other: Any?) = this === other || other is ParadoxParameterConditionExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
