package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
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
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val oldNode = context.node
        val isKey = context.isKey
        
        context.isKey = null
        
        val offset = context.offsetInParent!! - context.expressionOffset
        if(offset < 0) return //unexpected
        for(node in nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if(node is ParadoxDatabaseObjectTypeNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    ParadoxCompletionManager.completeDatabaseObjectType(context, resultToUse)
                    break
                }
            } else if(node is ParadoxDatabaseObjectNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    ParadoxCompletionManager.completeDatabaseObject(context, resultToUse)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.node = oldNode
        context.isKey = isKey
    }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
            if(expressionString.isEmpty()) return null
            
            val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxDatabaseObjectExpression(expressionString, range, nodes, configGroup)
            run r1@{
                val offset = range.startOffset
                val colonIndex1 = expressionString.indexOf(':')
                if(colonIndex1 == -1 && !incomplete) return null
                run r2@{
                    val nodeText = if(colonIndex1 == -1) expressionString else expressionString.substring(0, colonIndex1)
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxDatabaseObjectTypeNode.resolve(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
                if(colonIndex1 == -1) return@r1
                run r2@{ 
                    val node = ParadoxMarkerNode(":", TextRange.from(colonIndex1, 1))
                    nodes += node
                }
                val colonIndex2 = expressionString.indexOf(':', colonIndex1 + 1)
                run r2@{
                    val nodeText = if(colonIndex2 == -1) expressionString.substring(colonIndex1 + 1) else expressionString.substring(colonIndex1 + 1, colonIndex2)
                    val nodeTextRange = TextRange.from(colonIndex1 + 1, nodeText.length)
                    val node = ParadoxDatabaseObjectNode(nodeText, nodeTextRange, expression, 0)
                    nodes += node
                }
                if(colonIndex2 == -1) return@r1
                run r2@{
                    val node = ParadoxMarkerNode(":", TextRange.from(colonIndex2, 1))
                    nodes += node
                }
                run r2@{ 
                    val nodeText = expressionString.substring(colonIndex2 + 1)
                    val nodeTextRange = TextRange.from(colonIndex2 + 1, nodeText.length)
                    val node = ParadoxDatabaseObjectNode(nodeText, nodeTextRange, expression, 1)
                    nodes += node
                }
            }
            return expression
        }
    }
}

