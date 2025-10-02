package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefinePrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.util.PlsCoreManager

internal class ParadoxDefineReferenceExpressionResolverImpl : ParadoxDefineReferenceExpression.Resolver {
    override fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxDefineReferenceExpressionImpl(text, range, nodes, configGroup)

        run r1@{
            val offset = range.startOffset
            val prefix = "define:"
            if (text.startsWith(prefix)) {
                val node = ParadoxDefinePrefixNode(prefix, TextRange.from(offset, prefix.length), configGroup)
                nodes += node
            } else {
                if (!incomplete) return null
                val nodeTextRange = TextRange.from(offset, text.length)
                val node = ParadoxErrorTokenNode(text, nodeTextRange, configGroup)
                nodes += node
                return@r1
            }
            val pipeIndex = text.indexOf('|', prefix.length)
            run r2@{
                val nodeText = if (pipeIndex == -1) text.substring(prefix.length) else text.substring(prefix.length, pipeIndex)
                val nodeTextRange = TextRange.from(offset + prefix.length, nodeText.length)
                val node = ParadoxDefineNamespaceNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                nodes += node
            }
            if (pipeIndex == -1) return@r1
            run r2@{
                val nodeTextRange = TextRange.from(offset + pipeIndex, 1)
                val node = ParadoxMarkerNode("|", nodeTextRange, configGroup)
                nodes += node
            }
            run r2@{
                val nodeText = text.substring(pipeIndex + 1)
                val nodeTextRange = TextRange.from(offset + pipeIndex + 1, nodeText.length)
                val node = ParadoxDefineVariableNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                nodes += node
            }
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxDefineReferenceExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionBase(), ParadoxDefineReferenceExpression {
    override val namespaceNode: ParadoxDefineNamespaceNode?
        get() = nodes.getOrNull(1)?.cast()
    override val variableNode: ParadoxDefineVariableNode?
        get() = nodes.getOrNull(3)?.cast()

    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDefineNamespaceNode -> it.text.isParameterAwareIdentifier()
                it is ParadoxDefineVariableNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result || nodes.size != 4
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedDefineReferenceExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxDefineReferenceExpression && text == other.text
    override fun hashCode() = text.hashCode()
}
