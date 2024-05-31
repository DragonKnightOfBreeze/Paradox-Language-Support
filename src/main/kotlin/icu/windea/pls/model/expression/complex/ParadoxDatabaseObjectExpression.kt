package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.nodes.*

/**
 * 动态值表达式。对应的CWT规则类型为[CwtDataTypeGroups.DynamicValue]。
 *
 * 语法：
 *
 * ```bnf
 * database_object_expression ::= prefix ":" database_object (":" swapped_database_object)?
 * prefix ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * database_object ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * swapped_database_object ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
 * ```
 *
 * 示例：
 *
 * * `civic:some_civic`
 * * `civic:some_civic:some_swapped_civic`
 */
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