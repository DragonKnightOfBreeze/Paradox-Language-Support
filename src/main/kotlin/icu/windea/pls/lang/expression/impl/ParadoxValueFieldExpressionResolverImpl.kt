package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.expression.ParadoxScriptValueExpression
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.expression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.expression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.expression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.expression.validateAllNodes
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxType

internal class ParadoxValueFieldExpressionResolverImpl : ParadoxValueFieldExpression.Resolver {
    override fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && expressionString.isEmpty()) return null

        //skip if text is a number
        if (isNumber(expressionString)) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

        //skip if text is a parameter with unary operator prefix
        if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(expressionString, parameterRanges)) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val offset = range.startOffset
        var isLast = false
        var index: Int
        var tokenIndex = -1
        var startIndex = 0
        val textLength = expressionString.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = expressionString.indexOf('.', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
            if (tokenIndex != -1 && expressionString.indexOf('@', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
            if (tokenIndex != -1 && expressionString.indexOf('|', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
            if (tokenIndex != -1 && expressionString.indexOf('(', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
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
            val nodeText = expressionString.substring(startIndex, tokenIndex)
            val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
            startIndex = tokenIndex + 1
            val node = when {
                isLast -> ParadoxValueFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                else -> ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
            }
            //handle mismatch situation
            if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
            nodes += node
            if (dotNode != null) nodes += dotNode
        }
        if (!incomplete && nodes.isEmpty()) return null
        return ParadoxValueFieldExpressionImpl(expressionString, range, nodes, configGroup)
    }

    private fun isNumber(text: String): Boolean {
        return ParadoxScriptExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
    }
}

private class ParadoxValueFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxValueFieldExpression {
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
