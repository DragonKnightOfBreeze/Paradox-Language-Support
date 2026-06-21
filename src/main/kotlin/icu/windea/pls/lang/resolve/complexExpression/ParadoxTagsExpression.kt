package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator

/**
 * 标签集合表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.Tags]。
 *
 * TODO 2.1.10
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

private class ParadoxTagsExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxTagsExpression {
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxTagsExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}

// endregion
