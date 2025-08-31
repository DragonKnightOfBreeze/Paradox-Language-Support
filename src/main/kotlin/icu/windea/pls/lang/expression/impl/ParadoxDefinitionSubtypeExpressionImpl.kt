package icu.windea.pls.lang.expression.impl

import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo

internal class ParadoxDefinitionSubtypeExpressionResolver : ParadoxDefinitionSubtypeExpression.Resolver {
    override fun resolve(expressionString: String): ParadoxDefinitionSubtypeExpression {
        return ParadoxDefinitionSubtypeExpressionImpl(expressionString)
    }
}

private class ParadoxDefinitionSubtypeExpressionImpl(
    override val expressionString: String
) : ParadoxDefinitionSubtypeExpression {
    override val subtypes: List<ReversibleValue<String>> = expressionString.split('&').map { ReversibleValue(it) }

    override fun matches(subtypes: Collection<String>): Boolean {
        //目前仅支持"!"和"&"的组合
        return this.subtypes.all { t -> t.withOperator { subtypes.contains(it) } }
    }

    override fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.subtypes)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionSubtypeExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
