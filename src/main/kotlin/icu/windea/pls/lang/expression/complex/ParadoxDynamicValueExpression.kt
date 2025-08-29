package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cast
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.expression.complex.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.complex.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.expression.complex.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.expression.complex.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager

/**
 * 动态值表达式。对应的CWT规则类型为[CwtDataTypeGroups.DynamicValue]。
 *
 * 语法：
 *
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * dynamic_value ::= TOKEN //matching config expression "value[xxx]" or "value_set[xxx]"
 * //"event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 *
 * 示例：
 *
 * * `some_variable`
 * * `some_variable@root`
 */
class ParadoxDynamicValueExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val configs: List<CwtConfig<*>>
) : ParadoxComplexExpression.Base() {
    val dynamicValueNode: ParadoxDynamicValueNode
        get() = nodes.get(0).cast()
    val scopeFieldExpression: ParadoxScopeFieldExpression?
        get() = nodes.getOrNull(2)?.cast()

    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDynamicValueNode -> it.text.isParameterAwareIdentifier('.') //兼容点号
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionError.Builder.malformedDynamicValueExpression(rangeInExpression, text)
        return errors
    }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? {
            return resolve(expressionString, range, configGroup, config.singleton.list())
        }

        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
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
}
