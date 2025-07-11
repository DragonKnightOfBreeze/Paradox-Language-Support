package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*

/**
 * 数据库对象表达式。对应的CWT规则类型为[CwtDataTypes.DatabaseObject]。
 *
 * 也可以在本地化文件中作为概念名称使用。（如，`['civic:some_civic', ...]`）
 *
 * 语法：
 *
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object (":" swapped_database_object)?
 * database_object_type ::= TOKEN //predefined by CWT Config (see database_object_types.cwt)
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
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDatabaseObjectNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result || (nodes.size != 3 && nodes.size != 5)
        if (malformed) errors += ParadoxComplexExpressionError.Builder.malformedDatabaseObjectExpression(rangeInExpression, text)
        return errors
    }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
            val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxDatabaseObjectExpression(expressionString, range, nodes, configGroup)
            run r1@{
                val offset = range.startOffset
                val colonIndex1 = expressionString.indexOf(':')
                if (colonIndex1 == -1 && !incomplete) return null
                run r2@{
                    val nodeText = if (colonIndex1 == -1) expressionString else expressionString.substring(0, colonIndex1)
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxDatabaseObjectTypeNode.resolve(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
                if (colonIndex1 == -1) return@r1
                run r2@{
                    val nodeTextRange = TextRange.from(offset + colonIndex1, 1)
                    val node = ParadoxMarkerNode(":", nodeTextRange, configGroup)
                    nodes += node
                }
                val colonIndex2 = expressionString.indexOf(':', colonIndex1 + 1)
                run r2@{
                    val nodeText = if (colonIndex2 == -1) expressionString.substring(colonIndex1 + 1) else expressionString.substring(colonIndex1 + 1, colonIndex2)
                    val nodeTextRange = TextRange.from(offset + colonIndex1 + 1, nodeText.length)
                    val node = ParadoxDatabaseObjectNode.resolve(nodeText, nodeTextRange, configGroup, expression, isBase = true)
                    nodes += node
                }
                if (colonIndex2 == -1) return@r1
                run r2@{
                    val nodeTextRange = TextRange.from(offset + colonIndex2, 1)
                    val node = ParadoxMarkerNode(":", nodeTextRange, configGroup)
                    nodes += node
                }
                run r2@{
                    val nodeText = expressionString.substring(colonIndex2 + 1)
                    val nodeTextRange = TextRange.from(offset + colonIndex2 + 1, nodeText.length)
                    val node = ParadoxDatabaseObjectNode.resolve(nodeText, nodeTextRange, configGroup, expression, isBase = false)
                    nodes += node
                }
            }
            if (!incomplete && nodes.isEmpty()) return null
            return expression
        }
    }
}
