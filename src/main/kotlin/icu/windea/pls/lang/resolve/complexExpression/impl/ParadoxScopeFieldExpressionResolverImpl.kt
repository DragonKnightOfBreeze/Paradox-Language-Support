package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.util.ParadoxExpressionManager

internal class ParadoxScopeFieldExpressionResolverImpl : ParadoxScopeFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? {
        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxScopeFieldExpressionImpl(text, range, configGroup, nodes)

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
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxScopeFieldExpression {
    override val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()

    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxScopeFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
