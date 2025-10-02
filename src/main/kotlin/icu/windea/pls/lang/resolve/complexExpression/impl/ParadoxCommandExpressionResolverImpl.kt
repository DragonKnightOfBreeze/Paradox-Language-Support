package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.util.ParadoxExpressionManager

internal class ParadoxCommandExpressionResolverImpl : ParadoxCommandExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression? {
        if (text.isEmpty()) return null

        // val incomplete = PlsStates.incompleteComplexExpression.get() ?: false

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxCommandExpressionImpl(text, range, nodes, configGroup)
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
                nodes += node
            }
        }
        nodes += suffixNodes
        expression.finishResolving()
        return expression
    }
}

private class ParadoxCommandExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionBase(), ParadoxCommandExpression {
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
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedCommandExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxCommandExpression && text == other.text
    override fun hashCode() = text.hashCode()
}
