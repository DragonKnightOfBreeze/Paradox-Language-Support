package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.util.ParadoxExpressionManager

/**
 * 脚本值表达式。
 *
 * 说明：
 * - 作为 [ParadoxValueFieldExpression] 的一部分。
 *
 * 示例：
 * ```
 * some_sv
 * some_sv|PARAM|VALUE|
 * ```
 *
 * 语法：
 * ```bnf
 * script_value_expression ::= script_value script_value_args?
 * private script_value_args ::= "|" (script_value_argument "|" script_value_argument_value "|")+
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 以 `|` 为分隔，将文本拆分为：脚本值名、后续若干“参数名/参数值”对。
 * - 允许仅有脚本值名，或脚本值名后跟一个或多个成对出现的参数（`|name|value|`）。
 *
 * #### 节点组成
 * - 脚本值名：[ParadoxScriptValueNode]（第 1 段）。
 * - 参数名/参数值：成对出现，分别为 [ParadoxScriptValueArgumentNode] 与 [ParadoxScriptValueArgumentValueNode]（后续各段）。
 *
 * #### 解析与约束
 * - 参数名需为合法标识符；脚本值名需为可识别的标识符（允许参数变量）。
 * - 管道数量应为 0 或奇数（≥3）。仅 1 个或非零偶数个 `|` 将被视为格式错误。
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression?
    }

    companion object : Resolver by ParadoxScriptValueExpressionResolverImpl()
}

// region Implementations

private class ParadoxScriptValueExpressionResolverImpl : ParadoxScriptValueExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxScriptValueExpressionImpl(text, range, configGroup, config, nodes)

        val offset = range.startOffset
        var n = 0
        var valueNode: ParadoxScriptValueNode? = null
        var argumentNode: ParadoxScriptValueArgumentNode? = null
        var index: Int
        var tokenIndex = -1
        var startIndex = 0
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('|', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
            val pipeNode = if (tokenIndex != -1) {
                val pipeRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                ParadoxMarkerNode("|", pipeRange, configGroup)
            } else {
                null
            }
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            if (!incomplete && index == tokenIndex && tokenIndex == textLength) break
            // resolve node
            val nodeText = text.substring(startIndex, tokenIndex)
            val nodeRange = TextRange.create(startIndex + offset, tokenIndex + offset)
            startIndex = tokenIndex + 1
            val node = when {
                n == 0 -> {
                    ParadoxScriptValueNode.resolve(nodeText, nodeRange, configGroup, config)
                        .also { valueNode = it }
                }
                n % 2 == 1 -> {
                    ParadoxScriptValueArgumentNode.resolve(nodeText, nodeRange, configGroup, valueNode)
                        .also { argumentNode = it }
                }
                n % 2 == 0 -> {
                    ParadoxScriptValueArgumentValueNode.resolve(nodeText, nodeRange, configGroup, valueNode, argumentNode)
                }
                else -> throw InternalError()
            }
            nodes += node
            if (pipeNode != null) nodes += pipeNode
            n++
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxScriptValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val config: CwtConfig<*>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxScriptValueExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxScriptValueExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
