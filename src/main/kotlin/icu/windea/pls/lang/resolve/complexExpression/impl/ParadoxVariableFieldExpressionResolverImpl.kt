package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.linksOfVariable
import icu.windea.pls.core.cast
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxType

internal class ParadoxVariableFieldExpressionResolverImpl : ParadoxVariableFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        // skip if text is a number
        if (isNumber(text)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        // skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(text, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxVariableFieldExpressionImpl(text, range, configGroup, nodes)

        val offset = range.startOffset
        var startIndex = 0
        var i = 0
        var depthParen = 0
        var barrier = false // '@' 或 '|' 作为屏障：之后不再按 '.' 切分
        val textLength = text.length
        while (i < textLength) {
            val ch = text[i]
            val inParam = parameterRanges.any { i in it }
            if (!inParam) {
                when (ch) {
                    '(' -> depthParen++ // 支持 relations(x).owner
                    ')' -> if (depthParen > 0) depthParen--
                    '@', '|' -> if (depthParen == 0) barrier = true
                    '.' -> if (depthParen == 0 && !barrier) {
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
        // 最后一段：变量数据源
        run {
            val end = textLength
            val nodeText = text.substring(startIndex, end)
            val nodeTextRange = TextRange.create(startIndex + offset, end + offset)
            val node = ParadoxDataSourceNode.resolve(nodeText, nodeTextRange, configGroup, configGroup.linksOfVariable)
            // if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
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

private class ParadoxVariableFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxVariableFieldExpression {
    override val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
    override val variableNode: ParadoxDataSourceNode
        get() = nodes.last().cast()

    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedVariableFieldExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxVariableFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
