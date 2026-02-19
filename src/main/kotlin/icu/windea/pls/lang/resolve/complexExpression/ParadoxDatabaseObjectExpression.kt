package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName

/**
 * 数据库对象表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DatabaseObject]。
 * - 可以在本地化文件中作为概念名称（[ParadoxLocalisationConceptName]）使用。
 *
 * 示例：
 * ```
 * civic:some_civic
 * civic:some_civic:some_swapped_civic
 * job:job_soldier
 * ```
 *
 * 语法：
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object (":" database_object)?
 * database_object ::= database_object_data
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 形如：`<type>:<value>`，或 `<type>:<baseValue>:<swappedValue>`（允许一个可选的“替换值”段）。
 * - `<type>` 与 `<value>` 均来自 CWT 规则中预定义的数据库对象类型与对象名。
 *
 * #### 节点组成
 * - 类型节点：[ParadoxDatabaseObjectTypeNode]（第 1 段）。
 * - 值节点：[ParadoxDatabaseObjectNode]（第 2 段，基础值）。
 * - 可选替换值节点：[ParadoxDatabaseObjectNode]（第 3 段，替换值）。
 */
interface ParadoxDatabaseObjectExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression?
    }

    companion object : Resolver by ParadoxDataObjectExpressionResolverImpl()
}

// region Implementations

private class ParadoxDataObjectExpressionResolverImpl : ParadoxDatabaseObjectExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxDatabaseObjectExpressionImpl(text, range, configGroup, nodes)

        run r1@{
            val offset = range.startOffset
            val colonIndex1 = text.indexOf(':')
            if (colonIndex1 == -1 && !incomplete) return null
            run r2@{
                val nodeText = if (colonIndex1 == -1) text else text.substring(0, colonIndex1)
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
            val colonIndex2 = text.indexOf(':', colonIndex1 + 1)
            run r2@{
                val nodeText = if (colonIndex2 == -1) text.substring(colonIndex1 + 1) else text.substring(colonIndex1 + 1, colonIndex2)
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
                val nodeText = text.substring(colonIndex2 + 1)
                val nodeTextRange = TextRange.from(offset + colonIndex2 + 1, nodeText.length)
                val node = ParadoxDatabaseObjectNode.resolve(nodeText, nodeTextRange, configGroup, expression, isBase = false)
                nodes += node
            }
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxDatabaseObjectExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDatabaseObjectExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDatabaseObjectExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
