package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.model.expression.complex.nodes.*

interface ParadoxGameObjectExpression: ParadoxComplexExpression {
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? =
            doResolve(expression, range, configGroup)
    }
}

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
    TODO()
}

private class ParadoxGameObjectExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxGameObjectExpression {
    override fun validate(): List<ParadoxExpressionError> {
        TODO()
    }
    
    private fun isValid(node: ParadoxExpressionNode): Boolean {
        return node.text.isExactParameterAwareIdentifier()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //val contextElement = context.contextElement!!
        //val keyword = context.keyword
        //val keywordOffset = context.keywordOffset
        //val offsetInParent = context.offsetInParent!!
        //val isKey = context.isKey
        //
        //context.isKey = null
        //
        //TODO
        //
        //context.keyword = keyword
        //context.keywordOffset = keywordOffset
        //context.isKey = isKey
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxGameObjectExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}