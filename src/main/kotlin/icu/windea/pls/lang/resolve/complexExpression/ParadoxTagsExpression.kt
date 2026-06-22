package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidatorScope

/**
 * 标签集合表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.Tags]。
 * - 由逗号分隔的一组动态值节点组成（如 `tag` `tag1,tag2`），在条件变体下，可对其中的动态值节点进行取反（如 `tag1,not(tag2)`）。
 * - 兼容空白，也兼容逗号之间不存在任何字符或仅存在空白的情况。
 * - 需要用双引号括起，否则会给出警告。
 *
 * 节点组成：
 * - [ParadoxDynamicValueNode] - 标识符节点，匹配动态值，通常是某种标签（tag）。
 * - [ParadoxNegatedDynamicValueNode] - 复合节点，可包含动态值节点。形如 `not(tag)`。
 * - [ParadoxMarkerNode] - 对应其中的 `,` `(` `)`。
 * - [ParadoxBlankNode] - 对应其中的空白。
 *
 * 示例：
 * ```
 * tag
 * tag1,tag2
 * tag1,not(tag2)
 * ```
 *
 * 语法：
 * ```bnf
 * tags_expression ::= tag ("," tag)?
 * private tag ::= dynamic_value | invert_dynamic_value
 * invert_dynamic_value ::= "(" dynamic_value ")"
 * ```
 */
interface ParadoxTagsExpression : ParadoxComplexExpression {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxTagsExpression? {
            return ParadoxTagsExpressionResolver.resolve(text, range, configGroup)
        }
    }
}

// region Implementations

private object ParadoxTagsExpressionResolver {
    fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup): ParadoxTagsExpression? {
        val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxTagsExpressionImpl(text, range, configGroup, nodes)

        // TODO 2.1.10

        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolution()
        return expression
    }
}

private object ParadoxTagsExpressionValidator : ParadoxComplexExpressionValidatorScope {
    @Suppress("UNUSED_PARAMETER")
    fun validate(expression: ParadoxTagsExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) { if (it is ParadoxIdentifierNode) it.text.isParameterAwareIdentifier() else true }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedTagsExpression(expression.rangeInExpression, expression.text)
        checkQuotesForTagsExpression(element, expression, errors)
        return errors
    }
}

private class ParadoxTagsExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxTagsExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxTagsExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxTagsExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
