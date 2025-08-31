package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.expression.ParadoxCommandExpression
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.expression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxCommandSuffixNode
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.expression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.expression.validateAllNodes
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.util.ParadoxExpressionManager

internal class ParadoxCommandExpressionResolverImpl : ParadoxCommandExpression.Resolver {
    override fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression? {
        if (expressionString.isEmpty()) return null

        //val incomplete = PlsStates.incompleteComplexExpression.get() ?: false

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxCommandExpressionImpl(expressionString, range, nodes, configGroup)
        val suffixNodes = mutableListOf<ParadoxComplexExpressionNode>()
        var suffixStartIndex: Int
        run r1@{
            run r2@{
                suffixStartIndex = expressionString.indexOf('&')
                if (suffixStartIndex == -1) return@r2
                run r3@{
                    val node = ParadoxMarkerNode("&", TextRange.from(suffixStartIndex, 1), configGroup)
                    suffixNodes += node
                }
                run r3@{
                    val nodeText = expressionString.substring(suffixStartIndex + 1)
                    val node = ParadoxCommandSuffixNode.resolve(nodeText, TextRange.from(suffixStartIndex + 1, nodeText.length), configGroup)
                    suffixNodes += node
                }
                return@r1
            }
            run r2@{
                suffixStartIndex = expressionString.indexOf("::")
                if (suffixStartIndex == -1) return@r2
                run r3@{
                    val node = ParadoxMarkerNode("::", TextRange.from(suffixStartIndex, 2), configGroup)
                    suffixNodes += node
                }
                run r3@{
                    val nodeText = expressionString.substring(suffixStartIndex + 2)
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
            val expressionString0 = if (suffixStartIndex == -1) expressionString else expressionString.substring(0, suffixStartIndex)
            val textLength = expressionString0.length
            while (tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString0.indexOf('.', index)
                if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
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
        return expression
    }
}

private class ParadoxCommandExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxCommandExpression {
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
    override fun toString() = text
}
