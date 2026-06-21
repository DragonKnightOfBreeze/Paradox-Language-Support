package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidatorScope

/**
 * 定值引用表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DefineReference]。
 * - 引用的定值变量的值应当是一个字面量（通常是数字、颜色或日期）。
 * - 评估结果应是一个字面量（目前兼容数字字面量和字符串字面量）。
 *
 * 节点组成：
 * - [ParadoxDefineNamespaceNode] - 标识符节点，匹配定值命名空间（来自脚本文件）。
 * - [ParadoxDefineVariableNode] - 标识符节点，匹配定值变量（来自脚本文件）。
 * - [ParadoxMarkerNode] - 对应其中的 `|`。
 *
 * 示例：
 * ```
 * Namespace|Name
 * ```
 *
 * 语法：
 * ```bnf
 * define_reference_expression ::= define_namespace "|" define_variable
 * ```
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
            return ParadoxDefineReferenceExpressionResolver.resolve(text, range, configGroup)
        }
    }
}

// region Implementations

private object ParadoxDefineReferenceExpressionResolver {
    fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
        val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxDefineReferenceExpressionImpl(text, range, configGroup, nodes)

        run r1@{
            var namespaceNode: ParadoxDefineNamespaceNode? = null

            val offset = range.startOffset
            val pipeIndex1 = text.indexOf('|')
            if (pipeIndex1 == -1 && !incomplete) return null
            run r2@{
                val nodeText = if (pipeIndex1 == -1) text else text.substring(0, pipeIndex1)
                val nodeTextRange = TextRange.from(offset, nodeText.length)
                val node = ParadoxDefineNamespaceNode.resolve(nodeText, nodeTextRange, configGroup)
                nodes += node
                namespaceNode = node
            }
            if (pipeIndex1 == -1) return@r1
            run r2@{
                val nodeTextRange = TextRange.from(offset + pipeIndex1, 1)
                val node = ParadoxMarkerNode("|", nodeTextRange, configGroup)
                nodes += node
            }
            run r2@{
                val nodeText = text.substring(pipeIndex1 + 1)
                val nodeTextRange = TextRange.from(offset + pipeIndex1 + 1, nodeText.length)
                val node = ParadoxDefineVariableNode.resolve(nodeText, nodeTextRange, configGroup, namespaceNode)
                nodes += node
            }
        }

        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolution()
        return expression
    }
}

private object ParadoxDefineReferenceExpressionValidator : ParadoxComplexExpressionValidatorScope {
    @Suppress("UNUSED_PARAMETER")
    fun validate(expression: ParadoxDefineReferenceExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) { if (it is ParadoxIdentifierNode) it.text.isParameterAwareIdentifier() else true }
        val malformed = !result || expression.nodes.size != 3
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedDefineReferenceExpression(expression.rangeInExpression, expression.text)
        return errors
    }
}

private class ParadoxDefineReferenceExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDefineReferenceExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxDefineReferenceExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDefineReferenceExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
