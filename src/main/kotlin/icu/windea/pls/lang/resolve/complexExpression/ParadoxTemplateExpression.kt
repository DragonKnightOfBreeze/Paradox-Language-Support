package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtTemplateMatchService
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.hasState
import icu.windea.pls.lang.ParadoxThreadContext
import icu.windea.pls.lang.match.ParadoxTemplateMatchService
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

/**
 * 模板表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.Template]。
 * - 模板格式取决于对应的规则表达式（[CwtTemplateExpression]）。
 *
 * 语法：
 * ```bnf
 * template_expression ::= snippet+
 * private snippet ::= template_snippet_constant | template_snippet
 * template_snippet_constant ::= STRING_LITERAL
 * template_snippet ::= STRING_LITERAL
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
 * - 引用片段：[ParadoxTemplateSnippetNode]（与模板的引用部分对应）。
 *
 * #### 解析要点
 * - 将模板转为正则，对文本进行组匹配，再依组创建片段节点。
 * - 解析占位片段时，忽略匿名的定义。
 *
 * @see CwtTemplateExpression
 * @see ParadoxTemplateMatchService
 */
interface ParadoxTemplateExpression : ParadoxComplexExpression {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression? {
            return ParadoxTemplateExpressionResolver.resolve(text, range, configGroup, config)
        }
    }
}

// region Implementations

private object ParadoxTemplateExpressionResolver {
    fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression? {
        val templateExpression = when {
            config is CwtModifierConfig -> config.template
            else -> {
                val configExpression = config.configExpression ?: return null
                if (configExpression.type != CwtDataTypes.Template) return null
                val templateString = configExpression.expressionString
                CwtTemplateExpression.resolve(templateString)
            }
        }
        if (templateExpression.expressionString.isEmpty()) return null // null -> invalid `templateExpression` -> unexpected -> fast return

        val incomplete = ParadoxThreadContext.incompleteComplexExpression.hasState()
        if (!incomplete && text.isEmpty()) return null

        // 3.0.4 partial match should be allowed for incomplete-mode
        val matchResult = CwtTemplateMatchService.match(text, templateExpression, incomplete) ?: return null
        val matchGroups = matchResult.groups
        if (!incomplete && matchGroups.size < templateExpression.snippetExpressions.size) return null // unexpected
        // NOTE 3.0.4 #430 post optimization: still match in incomplete-mode if the matched value is empty (where snippet data type is `CwtDataTypes.Definition`, or not)
        if (!incomplete && matchGroups.anyFast { it.expression.type != CwtDataTypes.Constant && it.value.isEmpty() }) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxTemplateExpressionImpl(text, range, configGroup, nodes)

        val offset = range.startOffset
        for (matchGroup in matchGroups) {
            val snippetExpression = matchGroup.expression
            val nodeText = matchGroup.value
            val nodeTextRange = TextRange.from(offset + matchGroup.offset, nodeText.length)
            val node = when {
                snippetExpression.type == CwtDataTypes.Constant -> {
                    ParadoxTemplateSnippetConstantNode.resolve(nodeText, nodeTextRange, configGroup, snippetExpression.expressionString)
                }
                else -> {
                    ParadoxTemplateSnippetNode(nodeText, nodeTextRange, configGroup, snippetExpression)
                }
            }
            nodes += node
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolution()
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
