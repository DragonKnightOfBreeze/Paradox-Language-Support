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

        var suffixStartIndex: Int
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
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val expressionString0 = if (suffixStartIndex == -1) text else text.substring(0, suffixStartIndex)
            val textLength = expressionString0.length
            while (tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString0.indexOf('.', index)
                if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
                if (tokenIndex == -1) tokenIndex = textLength
                run r2@{
                    val nodeText = expressionString0.substring(startIndex, tokenIndex)
                    val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                    startIndex = tokenIndex + 1
                    val node = when {
                        tokenIndex != textLength -> ParadoxCommandScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                        else -> ParadoxCommandFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                    }
                    nodes += node
                }
                run r2@{
                    if (tokenIndex == textLength) return@r2
                    val node = ParadoxOperatorNode(".", TextRange.from(tokenIndex, 1), configGroup)
                    nodes += node
                }
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
