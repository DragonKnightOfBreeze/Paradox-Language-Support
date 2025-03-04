package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*

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

    override val errors by lazy { validate() }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? {
            return resolve(expressionString, range, configGroup, config.toSingletonList())
        }

        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
            val incomplete = PlsManager.incompleteComplexExpression.get() ?: false
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
                val nodeText = expressionString.substring(0, tokenIndex)
                val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
                val node = ParadoxDynamicValueNode.resolve(nodeText, nodeTextRange, configGroup, configs)
                if (node == null) return null //unexpected
                nodes.add(node)
                if (tokenIndex != textLength) {
                    //resolve at token
                    val atNode = ParadoxMarkerNode("@", TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset), configGroup)
                    nodes.add(atNode)
                    //resolve scope expression
                    val expString = expressionString.substring(tokenIndex + 1)
                    val expTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
                    val expNode = ParadoxScopeFieldExpression.resolve(expString, expTextRange, configGroup)!!
                    nodes.add(expNode)
                }
                break
            }
            if (!incomplete && nodes.isEmpty()) return null
            return ParadoxDynamicValueExpression(expressionString, range, nodes, configGroup, configs)
        }

        private fun ParadoxDynamicValueExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            val context = ParadoxComplexExpressionProcessContext()
            val result = processAllNodesToValidate(errors, context) {
                when {
                    it is ParadoxDynamicValueNode -> it.text.isParameterAwareIdentifier('.') //兼容点号
                    else -> true
                }
            }
            val malformed = !result
            if (malformed) errors += ParadoxComplexExpressionErrors.malformedDynamicValueExpression(rangeInExpression, text)
            return errors
        }
    }
}
