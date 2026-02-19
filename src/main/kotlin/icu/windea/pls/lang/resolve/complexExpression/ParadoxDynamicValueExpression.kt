package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.util.ParadoxExpressionManager

/**
 * 动态值表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypeSets.DynamicValue]。
 *
 * 示例：
 * ```
 * some_variable
 * some_variable@root
 * ```
 *
 * 语法：
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由“动态值名”与可选的 `@` 后缀“作用域字段表达式”组成：`dynamic_value` 或 `dynamic_value@scope_field`。
 * - 文本按第一处 `@` 切分；`@` 之后整体交由 [ParadoxScopeFieldExpression] 解析。
 *
 * #### 节点组成
 * - 动态值名：[ParadoxDynamicValueNode]（首段）。
 * - 分隔符：`@`（[ParadoxMarkerNode]，可选）。
 * - 作用域字段表达式：[ParadoxScopeFieldExpression]（可选）。
 *
 * #### 解析与约束
 * - 动态值名需为参数感知的标识符（兼容 `.`）。
 * - 仅处理第一处 `@`；其后内容整体作为作用域字段表达式输入。
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression?
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression?
    }

    companion object : Resolver by ParadoxDynamicValueExpressionResolverImpl()
}

// region Implementations

private class ParadoxDynamicValueExpressionResolverImpl : ParadoxDynamicValueExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? {
        return resolve(text, range, configGroup, config.to.singletonList())
    }

    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
        if (configs.any { it.configExpression?.type !in CwtDataTypeSets.DynamicValue }) return null

        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxDynamicValueExpressionImpl(text, range, configGroup, configs, nodes)

        val offset = range.startOffset
        var index: Int
        var tokenIndex = -1
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('@', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            // resolve dynamicValueNode
            run {
                val nodeText = text.substring(0, tokenIndex)
                val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
                val node = ParadoxDynamicValueNode.resolve(nodeText, nodeTextRange, configGroup, configs) ?: return null
                nodes += node
            }
            if (tokenIndex != textLength) {
                run {
                    // resolve at token
                    val nodeTextRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    val node = ParadoxMarkerNode("@", nodeTextRange, configGroup)
                    nodes += node
                }
                run {
                    // resolve scope expression
                    val nodeText = text.substring(tokenIndex + 1)
                    val nodeTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
                    val node = ParadoxScopeFieldExpression.resolve(nodeText, nodeTextRange, configGroup)
                        ?: ParadoxErrorTokenNode(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
            }
            break
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxDynamicValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val configs: List<CwtConfig<*>>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDynamicValueExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDynamicValueExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
