package icu.windea.pls.lang.expression.impl

import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression.Companion.resolve
import icu.windea.pls.model.ParadoxDefinitionInfo

internal class ParadoxDefinitionTypeExpressionResolverImpl : ParadoxDefinitionTypeExpression.Resolver {
    override fun resolve(expressionString: String): ParadoxDefinitionTypeExpression {
        return ParadoxDefinitionTypeExpressionImpl(expressionString)
    }
}

private class ParadoxDefinitionTypeExpressionImpl(
    override val expressionString: String
) : ParadoxDefinitionTypeExpression {
    override val type: String
    override val subtypes: List<String>

    init {
        val dotIndex = expressionString.indexOf('.')
        type = if (dotIndex == -1) expressionString else expressionString.substring(0, dotIndex)
        subtypes = if (dotIndex == -1) emptyList() else expressionString.substring(dotIndex + 1).split('.')
    }

    override fun matches(type: String, subtypes: Collection<String>): Boolean {
        return type == this.type && subtypes.containsAll(this.subtypes)
    }

    override fun matches(typeExpression: String): Boolean {
        return matches(resolve(typeExpression))
    }

    override fun matches(typeExpression: ParadoxDefinitionTypeExpression): Boolean {
        return matches(typeExpression.type, typeExpression.subtypes)
    }

    override fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.type, definitionInfo.subtypes)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionTypeExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
