package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.ParadoxTemplateExpression
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.expression.nodes.ParadoxTemplateSnippetNode
import icu.windea.pls.lang.util.PlsCoreManager

internal class ParadoxTemplateExpressionResolverImpl : ParadoxTemplateExpression.Resolver {
    override fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression? {
        val templateExpression = when {
            config is CwtModifierConfig -> {
                config.template
            }
            else -> {
                if (config.configExpression?.type != CwtDataTypes.TemplateExpression) return null
                val templateString = config.configExpression?.expressionString ?: return null
                CwtTemplateExpression.resolve(templateString)
            }
        }.takeIf { it.expressionString.isNotEmpty() } ?: return null

        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && expressionString.isEmpty()) return null

        //这里需要允许部分匹配
        val (_, matchResult) = CwtTemplateExpressionManager.toMatchedRegex(templateExpression, expressionString, incomplete) ?: return null

        val matchGroups = matchResult.groups.drop(1)
        if (matchGroups.isEmpty()) return null
        if (matchGroups.size > templateExpression.referenceExpressions.size) return null
        if (!incomplete && matchGroups.size < templateExpression.referenceExpressions.size) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = ParadoxTemplateExpressionImpl(expressionString, range, nodes, configGroup)
        run r1@{
            val offset = range.startOffset
            var startIndex = 0
            for ((i, matchGroup) in matchGroups.withIndex()) {
                if (matchGroup == null) return null
                val matchRange = matchGroup.range
                if (matchRange.first != startIndex) {
                    val nodeText = expressionString.substring(startIndex, matchRange.first)
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
            if (startIndex < expressionString.length) {
                val nodeText = expressionString.substring(startIndex)
                val nodeTextRange = TextRange.from(offset, nodeText.length)
                val node = ParadoxTemplateSnippetConstantNode(nodeText, nodeTextRange, configGroup)
                nodes += node
            }
        }
        if (!incomplete && nodes.isEmpty()) return null
        return expression
    }
}

private class ParadoxTemplateExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxTemplateExpression {
    override val errors: List<ParadoxComplexExpressionError> get() = emptyList()

    override fun equals(other: Any?) = this === other || other is ParadoxTemplateExpression && text == other.text
    override fun hashCode(): Int = text.hashCode()
    override fun toString(): String = text
}
