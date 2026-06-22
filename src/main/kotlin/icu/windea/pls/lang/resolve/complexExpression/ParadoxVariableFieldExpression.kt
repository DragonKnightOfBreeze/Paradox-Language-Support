package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidatorScope
import icu.windea.pls.lang.util.ParadoxExpressionManager

/**
 * 变量字段表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeSets.VariableField]。
 * - 作为 [ParadoxValueFieldExpression] 的子集。相较之下，仅支持调用变量。
 * - 由零个或多个作用域链接节点（[ParadoxScopeNode]）以及一个数据源节点（[ParadoxDataSourceNode]）组成。之间用点号分隔。
 * - 作用域链接节点可以是系统链接（[[ParadoxSystemScopeNode]]）、静态链接（[ParadoxStaticScopeNode]）、动态链接（[ParadoxDynamicScopeNode]）或者带参数的链接（[ParadoxParameterizedScopeNode]）。
 * - 数据源节点的数据类型固定为 `value[variable]`。
 * - 动态链接可能是前缀形式（`prefix:ds`），也可能是传参形式（`prefix(x)`）。其中可能嵌套其他复杂表达式。
 * - 对于传参形式的动态链接，兼容多个传参（`prefix(x,y)`）和字面量传参（`prefix('s')`）。传入链式表达式时，需要整个用双引号括起。
 *
 * [ParadoxDynamicScopeNode] 的数据源的解析优先级：
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.DynamicValue]，则解析为 [ParadoxDynamicValueExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ScopeField]，则解析为 [ParadoxScopeFieldExpression]。
 * - 如果数据源表达式的数据类型属于 [CwtDataTypeSets.ValueField]，则解析为 [ParadoxValueFieldExpression]。
 * - 如果不是任何嵌套的复杂表达式，则解析为 [ParadoxDataSourceNode]。
 *
 * 示例：
 * ```
 * root.owner.some_variable
 * ```
 *
 * 语法：
 * ```bnf
 * variable_field_expression ::= (scope_link ".")* variable
 * scope_link ::= system_scope | scope | dynamic_scope_link | parameterized_scope_link
 * dynamic_scope_link ::= scope_link_with_prefix | scope_link_with_args
 * private scope_link_with_prefix ::= scope_link_prefix? scope_link_value
 * private scope_link_with_args ::= scope_link_prefix "(" scope_link_args ")"
 * private scope_link_args ::= scope_link_arg ("," scope_link_arg)* // = scope_link_value
 * private scope_link_arg ::= scope_link_value
 * scope_link_value ::= dynamic_value_expression | scope_field_expression | value_field_expression | data_source
 * private variable ::= data_source
 * ```
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression, ParadoxLinkedExpression {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
            return ParadoxVariableFieldExpressionResolver.resolve(text, range, configGroup)
        }
    }
}

// region Implementations

private object ParadoxVariableFieldExpressionResolver {
    fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
        val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        // skip if text is a number
        if (TextMatcher.matchesFloat(text)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        // skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(text, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxVariableFieldExpressionImpl(text, range, configGroup, nodes)

        val offset = range.startOffset
        var startIndex = 0
        var i = 0
        var depthParen = 0
        val barrierCheckIndex = text.lastIndexOf("value:").let { if (it == -1) 0 else it }
        var barrier = false // '|' 作为屏障：之后不再按 '.' 切分
        val textLength = text.length
        while (i < textLength) {
            val ch = text[i]
            val inParam = parameterRanges.any { i in it }
            if (!inParam) {
                when (ch) {
                    '(' -> depthParen++ // 支持 prefix(x).owner：括号内的点不切分
                    ')' -> if (depthParen > 0) depthParen--
                    '|' -> if (depthParen == 0 && i >= barrierCheckIndex) barrier = true
                    '.' -> if (depthParen == 0 && !barrier) {
                        // 中间段：按作用域链接解析
                        val nodeText = text.substring(startIndex, i)
                        val nodeTextRange = TextRange.create(startIndex + offset, i + offset)
                        val node = ParadoxScopeNode.resolve(nodeText, nodeTextRange, configGroup)
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
        // 最后一段：变量数据源
        run {
            val end = textLength
            val nodeText = text.substring(startIndex, end)
            val nodeTextRange = TextRange.create(startIndex + offset, end + offset)
            val node = ParadoxDataSourceNode.resolve(nodeText, nodeTextRange, configGroup, configGroup.linksModel.variable)
            // if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolution()
        return expression
    }
}

private object ParadoxVariableFieldExpressionValidator : ParadoxComplexExpressionValidatorScope {
    @Suppress("UNUSED_PARAMETER")
    fun validate(expression: ParadoxVariableFieldExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedVariableFieldExpression(expression.rangeInExpression, expression.text)
        checkQuotes(element, expression, errors)
        return errors
    }
}

private class ParadoxVariableFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxVariableFieldExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxVariableFieldExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxVariableFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
