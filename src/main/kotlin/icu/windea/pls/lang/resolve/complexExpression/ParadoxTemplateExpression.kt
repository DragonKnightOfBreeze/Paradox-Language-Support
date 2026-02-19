package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetNode

/**
 * 模板表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.TemplateExpression]。
 * - 模板由 CWT 规则提供（或由修正的 `template` 字段提供），本表达式文本按模板匹配并被切分为“常量片段/占位片段”。
 *
 * 语法：
 * ```bnf
 * template_expression ::= snippet+
 * private snippet ::= template_snippet_constant | template_snippet
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 表达式文本与模板按组匹配，顺序交替产生：常量片段与占位片段。
 * - 允许部分匹配（不完整代码场景）。
 *
 * #### 节点组成
 * - 常量片段：[ParadoxTemplateSnippetConstantNode]（与模板的常量部分对应）。
 * - 占位片段：[ParadoxTemplateSnippetNode]（与模板的引用/占位部分对应）。
 *
 * #### 解析要点
 * - 模板来源：若配置为修正（`CwtModifierConfig`），取其 `template`；否则取 `configExpression` 中的 [CwtDataTypes.TemplateExpression]。
 * - 通过 `toMatchedRegex` 将模板转为正则并对文本进行组匹配，再依组构造片段节点。
 */
interface ParadoxTemplateExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression?
    }

    companion object : Resolver by ParadoxTemplateExpressionResolverImpl()
}

// region Implementations

private class ParadoxTemplateExpressionResolverImpl : ParadoxTemplateExpression.Resolver {
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
        val (_, matchResult) = CwtConfigExpressionManager.toMatchedRegex(templateExpression, text, incomplete) ?: return null

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
                val matchValue = matchGroup.value
                val snippetExpression = templateExpression.referenceExpressions[i]
                if (matchValue.isEmpty() && snippetExpression.type == CwtDataTypes.Definition) return null // skip anonymous definitions
                val nodeText = matchValue
                val nodeTextRange = TextRange.from(offset + matchRange.first, nodeText.length)
                val node = ParadoxTemplateSnippetNode(nodeText, nodeTextRange, configGroup, snippetExpression)
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
    override fun equals(other: Any?) = this === other || other is ParadoxTemplateExpression && text == other.text
    override fun hashCode(): Int = text.hashCode()
    override fun toString() = text
}

// endregion
