package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefinePrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator

/**
 * 定值引用表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DefineReference]。
 *
 * 示例：
 * ```
 * define:NPortrait|GRACEFUL_AGING_START
 * ```
 *
 * 语法：
 * ```bnf
 * define_reference_expression ::= "define:" define_namespace "|" define_variable
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 固定以前缀 `define:` 开头，随后是命名空间与变量名，以 `|` 分隔：`define:<namespace>|<variable>`。
 *
 * #### 节点组成
 * - 命名空间：[ParadoxDefineNamespaceNode]（`common/defines` 下 .txt 的一级键）。
 * - 变量名：[ParadoxDefineVariableNode]（同文件的二级键）。
 */
interface ParadoxDefineReferenceExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression?
    }

    companion object : Resolver by ParadoxDefineReferenceExpressionResolverImpl()
}

// region Implementations

private class ParadoxDefineReferenceExpressionResolverImpl : ParadoxDefineReferenceExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxDefineReferenceExpressionImpl(text, range, configGroup, nodes)

        run r1@{
            val offset = range.startOffset
            val prefix = "define:"
            if (text.startsWith(prefix)) {
                val node = ParadoxDefinePrefixNode(prefix, TextRange.from(offset, prefix.length), configGroup)
                nodes += node
            } else {
                if (!incomplete) return null
                val nodeTextRange = TextRange.from(offset, text.length)
                val node = ParadoxErrorTokenNode(text, nodeTextRange, configGroup)
                nodes += node
                return@r1
            }
            val pipeIndex = text.indexOf('|', prefix.length)
            run r2@{
                val nodeText = if (pipeIndex == -1) text.substring(prefix.length) else text.substring(prefix.length, pipeIndex)
                val nodeTextRange = TextRange.from(offset + prefix.length, nodeText.length)
                val node = ParadoxDefineNamespaceNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                nodes += node
            }
            if (pipeIndex == -1) return@r1
            run r2@{
                val nodeTextRange = TextRange.from(offset + pipeIndex, 1)
                val node = ParadoxMarkerNode("|", nodeTextRange, configGroup)
                nodes += node
            }
            run r2@{
                val nodeText = text.substring(pipeIndex + 1)
                val nodeTextRange = TextRange.from(offset + pipeIndex + 1, nodeText.length)
                val node = ParadoxDefineVariableNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                nodes += node
            }
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxDefineReferenceExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDefineReferenceExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDefineReferenceExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
