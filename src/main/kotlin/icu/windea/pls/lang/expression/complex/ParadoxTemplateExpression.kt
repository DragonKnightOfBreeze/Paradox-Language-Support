package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configExpression.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*

/**
 * 模版表达式。对应的CWT规则类型为[CwtDataTypes.TemplateExpression]。
 */
class ParadoxTemplateExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression? {
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
            val expression = ParadoxTemplateExpression(expressionString, range, nodes, configGroup)
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
}
