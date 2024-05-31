package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.nodes.*

interface ParadoxDatabaseObjectExpression: ParadoxComplexExpression {
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? =
            doResolve(expression, range, configGroup)
    }
}

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
    TODO()
}

private class ParadoxDatabaseObjectExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxDatabaseObjectExpression {
    override fun validate(): List<ParadoxComplexExpressionError> {
        TODO()
    }
    
    private fun isValid(node: ParadoxComplexExpressionNode): Boolean {
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
        return this === other || other is ParadoxDatabaseObjectExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}