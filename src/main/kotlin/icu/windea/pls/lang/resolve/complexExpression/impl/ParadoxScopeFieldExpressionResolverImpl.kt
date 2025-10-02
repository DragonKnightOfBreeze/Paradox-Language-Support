package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager

internal class ParadoxScopeFieldExpressionResolverImpl : ParadoxScopeFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxScopeFieldExpressionImpl(text, range, nodes, configGroup)

        val offset = range.startOffset
        var startIndex = 0
        var i = 0
        var depthParen = 0
        var barrier = false // '@' 或 '|' 作为分段屏障：一旦遇到，则后续不再按 '.' 分段
        val textLength = text.length
        while (i < textLength) {
            val ch = text[i]
            val inParam = parameterRanges.any { i in it }
            if (!inParam) {
                when (ch) {
                    '(' -> depthParen++ // 支持 relations(x).owner：括号内的点不切分
                    ')' -> if (depthParen > 0) depthParen--
                    '@', '|' -> if (depthParen == 0) barrier = true // barrier 生效：余下视作最后一段
                    '.' -> if (depthParen == 0 && !barrier) {
                        // 命中可分段的点
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
        // 收尾：最后一段
        run {
            val end = textLength
            val nodeText = text.substring(startIndex, end)
            val nodeTextRange = TextRange.create(startIndex + offset, end + offset)
            val node = ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxScopeFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionBase(), ParadoxScopeFieldExpression {
    override val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()

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
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedScopeFieldExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxScopeFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
}
