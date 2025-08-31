package icu.windea.pls.lang.expression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.expression.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.expression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.expression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.expression.validateAllNodes
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager

class ParadoxDynamicValueExpressionResolverImpl : ParadoxDynamicValueExpression.Resolver {
    override fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? {
        return resolve(expressionString, range, configGroup, config.singleton.list())
    }

    override fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
        if (configs.any { it.configExpression?.type !in CwtDataTypeGroups.DynamicValue }) return null

        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && expressionString.isEmpty()) return null

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val offset = range.startOffset
        var index: Int
        var tokenIndex = -1
        val textLength = expressionString.length
        while (tokenIndex < textLength) {
            index = tokenIndex + 1
            tokenIndex = expressionString.indexOf('@', index)
            if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
            if (tokenIndex == -1) {
                tokenIndex = textLength
            }
            //resolve dynamicValueNode
            run {
                val nodeText = expressionString.substring(0, tokenIndex)
                val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
                val node = ParadoxDynamicValueNode.resolve(nodeText, nodeTextRange, configGroup, configs) ?: return null
                nodes += node
            }
            if (tokenIndex != textLength) {
                run {
                    //resolve at token
                    val nodeTextRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    val node = ParadoxMarkerNode("@", nodeTextRange, configGroup)
                    nodes += node
                }
                run {
                    //resolve scope expression
                    val nodeText = expressionString.substring(tokenIndex + 1)
                    val nodeTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
                    val node = ParadoxScopeFieldExpression.resolve(nodeText, nodeTextRange, configGroup)
                        ?: ParadoxErrorTokenNode(nodeText, nodeTextRange, configGroup)
                    nodes += node
                }
            }
            break
        }
        if (!incomplete && nodes.isEmpty()) return null
        return ParadoxDynamicValueExpression(expressionString, range, nodes, configGroup, configs)
    }
}

private class ParadoxDynamicValueExpression(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    override val configs: List<CwtConfig<*>>
) : ParadoxDynamicValueExpression {
    override val dynamicValueNode: ParadoxDynamicValueNode
        get() = nodes.get(0).cast()
    override val scopeFieldExpression: ParadoxScopeFieldExpression?
        get() = nodes.getOrNull(2)?.cast()

    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDynamicValueNode -> it.text.isParameterAwareIdentifier('.') //兼容点号
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedDynamicValueExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is ParadoxDynamicValueExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
