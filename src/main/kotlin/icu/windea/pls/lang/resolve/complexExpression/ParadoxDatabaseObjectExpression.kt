package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidatorScope
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName

/**
 * 数据库对象表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DatabaseObject]。
 * - 可以在本地化文件中作为概念名称（[ParadoxLocalisationConceptName]）使用。
 *
 * 节点组成：
 * - [ParadoxDatabaseObjectTypeNode] - 标识符节点，匹配数据库对象类型（来自规则文件）。
 * - [ParadoxDatabaseObjectValueNode] - 复合节点，可包含数据节点。
 * - [ParadoxDatabaseObjectNode] - 标识符节点，匹配定义、切换后的定义或者本地化（基于数据库对象类型）。
 * - [ParadoxMarkerNode] - 对应其中的 `:`。
 *
 * 示例：
 * ```
 * civic:some_civic # definition
 * civic:some_civic:some_swapped_civic # definition + swapped definition
 * job:job_soldier # localisation
 * ```
 *
 * 语法：
 * ```bnf
 * database_object_expression ::= database_object_type ":" database_object_value (":" database_object_value)?
 * database_object_value ::= database_object
 * ```
 */
interface ParadoxDatabaseObjectExpression : ParadoxComplexExpression {
    val typeNode: ParadoxDatabaseObjectTypeNode?
    val valueNode: ParadoxDatabaseObjectValueNode?
    val swappedValueNode: ParadoxDatabaseObjectValueNode?

    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
            return ParadoxDataObjectExpressionResolver.resolve(text, range, configGroup)
        }
    }
}

// region Implementations

private object ParadoxDataObjectExpressionResolver {
    fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDatabaseObjectExpression? {
        val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false
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
                val node = ParadoxDatabaseObjectValueNode.resolve(nodeText, nodeTextRange, configGroup, expression, isBase = true)
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
                val node = ParadoxDatabaseObjectValueNode.resolve(nodeText, nodeTextRange, configGroup, expression, isBase = false)
                nodes += node
            }
        }

        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolution()
        return expression
    }
}

private object ParadoxDatabaseObjectExpressionValidator : ParadoxComplexExpressionValidatorScope {
    @Suppress("UNUSED_PARAMETER")
    fun validate(expression: ParadoxDatabaseObjectExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val config = expression.typeNode?.config
        val result = validateAllNodes(expression, element, errors) {
            when {
                it is ParadoxDatabaseObjectNode -> {
                    when {
                        config?.localisation != null -> it.text.isParameterAwareIdentifier(".-'")
                        else -> it.text.isParameterAwareIdentifier()
                    }
                }
                else -> true
            }
        }
        val malformed = !result || (expression.nodes.size != 3 && expression.nodes.size != 5)
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedDatabaseObjectExpression(expression.rangeInExpression, expression.text)
        return errors
    }
}

private class ParadoxDatabaseObjectExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDatabaseObjectExpression {
    override val typeNode: ParadoxDatabaseObjectTypeNode?
        get() = nodes.getOrNull(0)?.castOrNull()
    override val valueNode: ParadoxDatabaseObjectValueNode?
        get() = nodes.getOrNull(2)?.castOrNull()
    override val swappedValueNode: ParadoxDatabaseObjectValueNode?
        get() = nodes.getOrNull(4)?.castOrNull()

    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxDatabaseObjectExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDatabaseObjectExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
