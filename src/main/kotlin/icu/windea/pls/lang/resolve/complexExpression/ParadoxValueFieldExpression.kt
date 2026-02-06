package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxParameterizedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxType

/**
 * 值字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeSets.ValueField]。
 * - 由零个或多个作用域链接节点（[ParadoxScopeLinkNode]）以及一个值字段节点（[ParadoxValueFieldNode]）组成。之间用点号分隔。
 * - 作用域链接节点可以是静态链接（[ParadoxSystemScopeNode] 和 [ParadoxScopeNode]）、动态链接（[ParadoxDynamicScopeLinkNode]）或者带参数的链接（[ParadoxParameterizedScopeLinkNode]）。
 * - 值字段链接节点可以是静态链接（[ParadoxPredefinedValueFieldNode]）、动态链接（[ParadoxDynamicValueFieldNode]）或者带参数的链接（[ParadoxParameterizedValueFieldNode]）。
 * - 动态链接可能是前缀形式（`prefix:ds`），也可能是传参形式（`prefix(x)`）。其中可能嵌套其他复杂表达式。
 * - 对于传参形式的动态链接，兼容多个传参（`prefix(x,y)`）和字面量传参（`prefix('s')`）。传入链式表达式时，需要整个用双引号括起。
 *
 * [ParadoxDynamicScopeLinkNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.DynamicValue]，则解析为 [ParadoxDynamicValueExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ScopeField]，则解析为 [ParadoxScopeFieldExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ValueField]，则解析为 [ParadoxValueFieldExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
 *
 * [ParadoxDynamicValueFieldNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.DynamicValue]，则解析为 [ParadoxDynamicValueExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ScopeField]，则解析为 [ParadoxScopeFieldExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ValueField]，则解析为 [ParadoxValueFieldExpression]。
 * - 如果数据源表达式是 `<script_value>`，则解析为 [ParadoxScriptValueExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
 *
 * 示例：
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * relations(root)
 * root.owner.some_variable
 * ```
 *
 * 语法：
 * ```bnf
 * value_field_expression ::= (scope_link ".")* value_field
 * scope_link ::= system_scope | scope | dynamic_scope_link | parameterized_scope_link
 * dynamic_scope_link ::= scope_link_with_prefix | scope_link_with_args
 * private scope_link_with_prefix ::= scope_link_prefix? scope_link_value
 * private scope_link_with_args ::= scope_link_prefix "(" scope_link_args ")"
 * private scope_link_args ::= scope_link_arg ("," scope_link_arg)* // = scope_link_value
 * private scope_link_arg ::= scope_link_value
 * scope_link_value ::= dynamic_value_expression | scope_field_expression | value_field_expression | data_source
 * value_field ::= predefined_value_field | dynamic_value_field | parameterized_value_field
 * dynamic_value_field ::= value_field_with_prefix | value_field_with_args
 * private value_field_with_prefix ::= value_field_prefix? value_field_value
 * private value_field_with_args ::= value_field_prefix "(" value_field_args ")"
 * private value_field_args ::= value_field_arg ("," value_field_arg)* // = value_field_value
 * private value_field_arg ::= value_field_value
 * value_field_value ::= dynamic_value_expression | scope_field_expression | value_field_expression | script_value_expression | data_source
 * ```
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression, ParadoxLinkedExpression {

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxValueFieldExpression?
    }

    companion object : Resolver by ParadoxValueFieldExpressionResolverImpl()
}

// region Implementations

private class ParadoxValueFieldExpressionResolverImpl : ParadoxValueFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        // skip if text is a number
        if (isNumber(text)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        // skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(text, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxValueFieldExpressionImpl(text, range, configGroup, nodes)

        val offset = range.startOffset
        var startIndex = 0
        var i = 0
        var depthParen = 0
        val barrierCheckIndex = text.lastIndexOf("value:").let { if (it == -1) 0 else it }
        var barrier = false // '@' 或 '|' 作为屏障：之后不再按 '.' 切分
        val textLength = text.length
        while (i < textLength) {
            val ch = text[i]
            val inParam = parameterRanges.any { i in it }
            if (!inParam) {
                when (ch) {
                    '(' -> depthParen++ // 支持 prefix(x).owner：括号内的点不切分
                    ')' -> if (depthParen > 0) depthParen--
                    '@', '|' -> if (depthParen == 0 && i >= barrierCheckIndex) barrier = true
                    '.' -> if (depthParen == 0 && !barrier) {
                        // 中间段：按作用域链接解析
                        val nodeText = text.substring(startIndex, i)
                        val nodeTextRange = TextRange.create(startIndex + offset, i + offset)
                        val node = ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
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
        // 最后一段：按值字段解析
        run {
            val end = textLength
            val nodeText = text.substring(startIndex, end)
            val nodeTextRange = TextRange.create(startIndex + offset, end + offset)
            val node = ParadoxValueFieldNode.resolve(nodeText, nodeTextRange, configGroup)
            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }

    private fun isNumber(text: String): Boolean {
        return ParadoxScriptExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
    }
}

private class ParadoxValueFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxValueFieldExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxValueFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
