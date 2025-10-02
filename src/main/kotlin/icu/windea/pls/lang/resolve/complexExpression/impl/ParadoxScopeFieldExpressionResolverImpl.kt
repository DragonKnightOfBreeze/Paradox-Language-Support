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
        var index: Int
        var tokenIndex = -1
        var startIndex = 0
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('.', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
            if (tokenIndex != -1 && text.indexOf('@', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
            if (tokenIndex != -1 && text.indexOf('|', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
            if (tokenIndex != -1 && text.indexOf('(', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
            val dotNode = if (tokenIndex != -1) {
                val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                ParadoxOperatorNode(".", dotRange, configGroup)
            } else {
                null
            }
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            // resolve node
            val nodeText = text.substring(startIndex, tokenIndex)
            val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
            startIndex = tokenIndex + 1
            val node = ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
            // handle mismatch situation
            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
            if (dotNode != null) nodes += dotNode
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
