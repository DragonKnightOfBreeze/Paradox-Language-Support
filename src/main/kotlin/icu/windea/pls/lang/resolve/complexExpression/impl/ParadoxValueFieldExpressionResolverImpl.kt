package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxType

internal class ParadoxValueFieldExpressionResolverImpl : ParadoxValueFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        // skip if text is a number
        if (isNumber(text)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        // skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(text, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
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
    override val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
    override val valueFieldNode: ParadoxValueFieldNode
        get() = nodes.last().cast()
    override val scriptValueExpression: ParadoxScriptValueExpression?
        get() = valueFieldNode.castOrNull<ParadoxDynamicValueFieldNode>()?.valueNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

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
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedValueFieldExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxValueFieldExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
