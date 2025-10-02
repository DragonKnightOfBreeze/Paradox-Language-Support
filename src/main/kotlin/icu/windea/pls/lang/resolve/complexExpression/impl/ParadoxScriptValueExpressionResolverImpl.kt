package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTokenNode
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager

internal class ParadoxScriptValueExpressionResolverImpl : ParadoxScriptValueExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxScriptValueExpressionImpl(text, range, nodes, configGroup, config)

        val offset = range.startOffset
        var n = 0
        var valueNode: ParadoxScriptValueNode? = null
        var argumentNode: ParadoxScriptValueArgumentNode? = null
        var index: Int
        var tokenIndex = -1
        var startIndex = 0
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('|', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
            val pipeNode = if (tokenIndex != -1) {
                val pipeRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                ParadoxMarkerNode("|", pipeRange, configGroup)
            } else {
                null
            }
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            if (!incomplete && index == tokenIndex && tokenIndex == textLength) break
            // resolve node
            val nodeText = text.substring(startIndex, tokenIndex)
            val nodeRange = TextRange.create(startIndex + offset, tokenIndex + offset)
            startIndex = tokenIndex + 1
            val node = when {
                n == 0 -> {
                    ParadoxScriptValueNode.resolve(nodeText, nodeRange, configGroup, config)
                        .also { valueNode = it }
                }
                n % 2 == 1 -> {
                    ParadoxScriptValueArgumentNode.resolve(nodeText, nodeRange, configGroup, valueNode)
                        .also { argumentNode = it }
                }
                n % 2 == 0 -> {
                    ParadoxScriptValueArgumentValueNode.resolve(nodeText, nodeRange, configGroup, valueNode, argumentNode)
                }
                else -> throw InternalError()
            }
            nodes += node
            if (pipeNode != null) nodes += pipeNode
            n++
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxScriptValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    override val config: CwtConfig<*>
) : ParadoxComplexExpressionBase(), ParadoxScriptValueExpression {
    override val scriptValueNode: ParadoxScriptValueNode
        get() = nodes.first().cast()
    override val argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>
        get() = buildList {
            var argumentNode: ParadoxScriptValueArgumentNode? = null
            for (node in nodes) {
                if (node is ParadoxScriptValueArgumentNode) {
                    argumentNode = node
                } else if (node is ParadoxScriptValueArgumentValueNode && argumentNode != null) {
                    add(tupleOf(argumentNode, node))
                    argumentNode = null
                }
            }
            if (argumentNode != null) {
                add(tupleOf(argumentNode, null))
            }
        }

    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxScriptValueNode -> it.text.isParameterAwareIdentifier()
                it is ParadoxScriptValueArgumentNode -> it.text.isIdentifier()
                it is ParadoxScriptValueArgumentValueNode -> true
                else -> true
            }
        }
        var malformed = !result
        if (!malformed) {
            // check whether pipe count is valid
            val pipeNodeCount = nodes.count { it is ParadoxTokenNode && it.text == "|" }
            if (pipeNodeCount == 1 || (pipeNodeCount != 0 && pipeNodeCount % 2 == 0)) {
                malformed = true
            }
        }
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedScriptValueExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxScriptValueExpression && text == other.text
    override fun hashCode() = text.hashCode()
}
