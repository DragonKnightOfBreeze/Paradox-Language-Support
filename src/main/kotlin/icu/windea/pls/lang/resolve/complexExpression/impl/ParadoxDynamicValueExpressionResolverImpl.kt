package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator
import icu.windea.pls.lang.util.ParadoxExpressionManager

internal class ParadoxDynamicValueExpressionResolverImpl : ParadoxDynamicValueExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? {
        return resolve(text, range, configGroup, config.singleton.list())
    }

    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
        if (configs.any { it.configExpression?.type !in CwtDataTypeGroups.DynamicValue }) return null

        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = ParadoxDynamicValueExpression(text, range, configGroup, configs, nodes)

        val offset = range.startOffset
        var index: Int
        var tokenIndex = -1
        val textLength = text.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = text.indexOf('@', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue // skip parameter text
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            // resolve dynamicValueNode
            run {
                val nodeText = text.substring(0, tokenIndex)
                val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
                val node = ParadoxDynamicValueNode.resolve(nodeText, nodeTextRange, configGroup, configs) ?: return null
                nodes += node
            }
            if (tokenIndex != textLength) {
                run {
                    // resolve at token
                    val nodeTextRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    val node = ParadoxMarkerNode("@", nodeTextRange, configGroup)
                    nodes += node
                }
                run {
                    // resolve scope expression
                    val nodeText = text.substring(tokenIndex + 1)
                    val nodeTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
                    val node = ParadoxScopeFieldExpression.resolve(nodeText, nodeTextRange, configGroup)
                        ?: ParadoxErrorTokenNode(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
            }
            break
        }
        if (!incomplete && nodes.isEmpty()) return null
        expression.finishResolving()
        return expression
    }
}

private class ParadoxDynamicValueExpression(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val configs: List<CwtConfig<*>>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), ParadoxDynamicValueExpression {
    override val dynamicValueNode: ParadoxDynamicValueNode
        get() = nodes.get(0).cast()
    override val scopeFieldExpression: ParadoxScopeFieldExpression?
        get() = nodes.getOrNull(2)?.cast()

    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is ParadoxDynamicValueExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
