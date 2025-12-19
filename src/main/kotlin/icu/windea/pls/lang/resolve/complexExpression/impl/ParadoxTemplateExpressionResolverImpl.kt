package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetNode

internal class ParadoxTemplateExpressionResolverImpl : ParadoxTemplateExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression? {
        val templateExpression = when {
            config is CwtModifierConfig -> config.template
            else -> {
                if (config.configExpression?.type != CwtDataTypes.TemplateExpression) return null
                val templateString = config.configExpression?.expressionString ?: return null
                CwtTemplateExpression.resolve(templateString)
            }
        }.takeIf { it.expressionString.isNotEmpty() } ?: return null

        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        // 这里需要允许部分匹配
        val (_, matchResult) = CwtTemplateExpressionManager.toMatchedRegex(templateExpression, text, incomplete) ?: return null

        val matchGroups = matchResult.groups.drop(1)
        if (matchGroups.isEmpty()) return null
        if (matchGroups.size > templateExpression.referenceExpressions.size) return null
        if (!incomplete && matchGroups.size < templateExpression.referenceExpressions.size) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxTemplateExpressionImpl(text, range, configGroup, nodes)

        run r1@{
            val offset = range.startOffset
            var startIndex = 0
            for ((i, matchGroup) in matchGroups.withIndex()) {
                if (matchGroup == null) return null
                val matchRange = matchGroup.range
                if (matchRange.first != startIndex) {
                    val nodeText = text.substring(startIndex, matchRange.first)
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxTemplateSnippetConstantNode(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
                val referenceExpression = templateExpression.referenceExpressions[i]
                val nodeText = matchGroup.value
                val nodeTextRange = TextRange.from(offset + matchRange.first, nodeText.length)
                val node = ParadoxTemplateSnippetNode(nodeText, nodeTextRange, configGroup, referenceExpression)
                nodes += node
                startIndex = matchRange.last + 1
            }
            if (startIndex < text.length) {
                val nodeText = text.substring(startIndex)
                val nodeTextRange = TextRange.from(offset, nodeText.length)
                val node = ParadoxTemplateSnippetConstantNode(nodeText, nodeTextRange, configGroup)
                nodes += node
            }
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxTemplateExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxTemplateExpression {
    override val errors: List<ParadoxComplexExpressionError> get() = emptyList()

    override fun equals(other: Any?) = this === other || other is ParadoxTemplateExpression && text == other.text
    override fun hashCode(): Int = text.hashCode()
    override fun toString() = text
}
