package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText

/**
 * （本地化）命令表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.DatabaseObject]。目前不支持用来匹配脚本表达式。
 * - 可以在本地化文件中作为命令文本（[ParadoxLocalisationCommandText]）使用。
 * - 由零个或多个命令作用域链接节点（[ParadoxCommandScopeLinkNode]）以及一个命令字段节点（[ParadoxCommandFieldNode]）组成。之间用点号分隔。之后可能还有其他额外的后缀节点。
 * - 命令作用域链接节点可以是静态链接（[ParadoxSystemCommandScopeNode] 和 [ParadoxCommandScopeNode]）、动态链接（[ParadoxDynamicCommandScopeLinkNode]）或者带参数的链接（[ParadoxParameterizedCommandScopeLinkNode]）。
 * - 值字段链接节点可以是静态链接（[ParadoxPredefinedCommandFieldNode]）、动态链接（[ParadoxDynamicCommandFieldNode]）或者带参数的链接（[ParadoxParameterizedCommandFieldNode]）。
 * - 动态链接可能是前缀形式（`Prefix:DS`），也可能是传参形式（`Prefix(X)`）。其中可能嵌套其他复杂表达式。
 * - 对于传参形式的动态链接，兼容多个传参（`Prefix(X,Y)`）和字面量传参（`Prefix('s')`）。
 *
 * [ParadoxDynamicCommandScopeLinkNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型是 [CwtDataTypes.Command]，则解析为 [ParadoxCommandExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
 *
 * [ParadoxDynamicCommandFieldNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型是 [CwtDataTypes.Command]，则解析为 [ParadoxCommandExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
 *
 * 示例：
 * ```
 * Root.GetName
 * Root.Owner.event_target:some_event_target.var
 * ```
 *
 * 语法：
 * ```bnf
 * command_expression ::= (command_scope_link ".")* command_field command_suffix?
 * command_scope_link ::= system_command_scope | command_scope | dynamic_command_scope_link | parameterized_command_scope_link
 * dynamic_command_scope_link ::= command_scope_link_with_prefix | command_scope_link_with_args
 * private command_scope_link_with_prefix ::= command_scope_link_prefix? command_scope_link_value
 * private command_scope_link_with_args ::= command_scope_link_prefix "(" command_scope_link_args ")"
 * private command_scope_link_args ::= command_scope_link_arg ("," command_scope_link_arg)* // = command_scope_link_value
 * private command_scope_link_arg ::= command_scope_link_value
 * command_scope_link_value ::= data_source
 * command_field ::= predefined_command_field | dynamic_command_field | parameterized_command_field
 * dynamic_command_field ::= command_field_with_prefix | command_field_with_args
 * private command_field_with_prefix ::= command_field_prefix? command_field_value
 * private command_field_with_args ::= command_field_prefix "(" command_field_args ")"
 * private command_field_args ::= command_field_arg ("," command_field_arg)* // = command_field_value
 * private command_field_arg ::= command_field_value
 * command_field_value ::= data_source
 * command_suffix ::= "&" SUFFIX | "::" SUFFIX
 * ```
 */
interface ParadoxCommandExpression : ParadoxComplexExpression, ParadoxLinkedExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxCommandExpression?
    }

    companion object : Resolver by ParadoxCommandExpressionResolverImpl()
}

// region Implementations

private class ParadoxCommandExpressionResolverImpl : ParadoxCommandExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxCommandExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxCommandExpressionImpl(text, range, configGroup, nodes)
        val suffixNodes = mutableListOf<ParadoxComplexExpressionNode>()

        var suffixStartIndex = -1
        run r1@{
            run r2@{
                suffixStartIndex = text.indexOf('&')
                if (suffixStartIndex == -1) return@r2
                run r3@{
                    val node = ParadoxMarkerNode("&", TextRange.from(suffixStartIndex, 1), configGroup)
                    suffixNodes += node
                }
                run r3@{
                    val nodeText = text.substring(suffixStartIndex + 1)
                    val node = ParadoxCommandSuffixNode.resolve(nodeText, TextRange.from(suffixStartIndex + 1, nodeText.length), configGroup)
                    suffixNodes += node
                }
                return@r1
            }
            run r2@{
                suffixStartIndex = text.indexOf("::")
                if (suffixStartIndex == -1) return@r2
                run r3@{
                    val node = ParadoxMarkerNode("::", TextRange.from(suffixStartIndex, 2), configGroup)
                    suffixNodes += node
                }
                run r3@{
                    val nodeText = text.substring(suffixStartIndex + 2)
                    val node = ParadoxCommandSuffixNode.resolve(nodeText, TextRange.from(suffixStartIndex + 2, nodeText.length), configGroup)
                    suffixNodes += node
                }
            }
        }
        run r1@{
            val offset = range.startOffset
            val expressionString0 = if (suffixStartIndex == -1) text else text.substring(0, suffixStartIndex)
            var startIndex = 0
            var i = 0
            var depthParen = 0
            val textLength = expressionString0.length
            while (i < textLength) {
                val ch = expressionString0[i]
                val inParam = parameterRanges.any { it.contains(i) }
                if (!inParam) {
                    when (ch) {
                        '(' -> depthParen++ // 括号内的点不切分
                        ')' -> if (depthParen > 0) depthParen--
                        '.' -> if (depthParen == 0) {
                            val nodeText = expressionString0.substring(startIndex, i)
                            val nodeTextRange = TextRange.create(startIndex + offset, i + offset)
                            val node = ParadoxCommandScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                            nodes += node
                            val dotRange = TextRange.create(i + offset, i + 1 + offset)
                            nodes += ParadoxOperatorNode(".", dotRange, configGroup)
                            startIndex = i + 1
                        }
                    }
                }
                i++
            }
            // last segment -> command field
            run {
                val end = textLength
                val nodeText = expressionString0.substring(startIndex, end)
                val nodeTextRange = TextRange.create(startIndex + offset, end + offset)
                val node = ParadoxCommandFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes += node
            }
        }
        nodes += suffixNodes
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxCommandExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxCommandExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxCommandExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
