package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.nodes.*

/**
 * 数据库对象表达式。对应的CWT规则类型为[CwtDataTypeGroups.DatabaseObject]。
 *
 * 也可以在本地化文件中作为概念名称使用。（如，`['civic:some_civic', ...]`）
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
class ParadoxDatabaseObjectExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    val typeNode: ParadoxDatabaseObjectTypeNode?
        get() = nodes.getOrNull(0)?.cast()
    val valueNode: ParadoxDatabaseObjectNode?
        get() = nodes.getOrNull(2)?.cast()
    val swapValueNode: ParadoxDatabaseObjectNode?
        get() = nodes.getOrNull(4)?.cast()
    
    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var malformed = false
        for(node in nodes) {
            if(node.text.isEmpty()) {
                malformed = true
                break
            }
        }
        if(malformed) {
            val error = ParadoxComplexExpressionErrors.malformedGameObjectExpression(rangeInExpression, text)
            errors.add(0, error)
        }
        return errors
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
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
            TODO()
            
            //if(expressionString.isEmpty()) return null
            //
            //val colonIndex1 = expressionString.indexOf(':')
            //if(colonIndex1 == -1) return null
            //val colonIndex2 = expressionString.indexOf(':', colonIndex1 + 1)
            //
            //val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            //val expression = ParadoxDatabaseObjectExpressionImpl(expressionString, range, nodes, configGroup)
            //
            //return expression
        }
    }
}

