package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.linksOfVariable
import icu.windea.pls.core.cast
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.expression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.expression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.expression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.expression.validateAllNodes
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxType

internal class  ParadoxVariableFieldExpressionResolverImpl: ParadoxVariableFieldExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        //skip if text is a number
        if (isNumber(text)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        //skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(text, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxVariableFieldExpressionImpl(text, range, nodes, configGroup)
        val offset = range.startOffset
        var isLast = false
        var index: Int
        var tokenIndex = -1
        var startIndex = 0
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('.', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
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
                isLast = true
            }
            //resolve node
            val nodeText = text.substring(startIndex, tokenIndex)
            val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
            startIndex = tokenIndex + 1
            val node = when {
                isLast -> ParadoxDataSourceNode.resolve(nodeText, nodeTextRange, configGroup, configGroup.linksOfVariable)
                else -> ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
            }
            //handle mismatch situation
            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
            if (dotNode != null) nodes += dotNode
        }
        if (!incomplete && nodes.isEmpty()) return null
        return expression
    }

    private fun isNumber(text: String): Boolean {
        return ParadoxScriptExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
    }
}

private class ParadoxVariableFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxVariableFieldExpression {
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
