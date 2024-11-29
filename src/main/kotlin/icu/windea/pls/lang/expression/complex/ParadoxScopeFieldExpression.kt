package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*

/**
 * 作用域字段表达式。对应的CWT规则类型为[CwtDataTypeGroups.ScopeField]。
 *
 * 语法：
 *
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value //predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= EXPRESSION //e.g. "some_variable" while the link's data source is "value[variable]"
 * expression ::= data_expression | dynamic_value_expression //see: ParadoxDataExpression, ParadoxDynamicValueExpression
 * ```
 *
 * 示例：
 *
 * * `root`
 * * `root.owner`
 * * `event_target:some_target`
 */
class ParadoxScopeFieldExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()

    override val errors by lazy { validate() }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = range.startOffset
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val textLength = expressionString.length
            while (tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString.indexOf('.', index)
                if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                if (tokenIndex != -1 && expressionString.indexOf('@', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                if (tokenIndex != -1 && expressionString.indexOf('|', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                val dotNode = if (tokenIndex != -1) {
                    val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxOperatorNode(".", dotRange, configGroup)
                } else {
                    null
                }
                if (tokenIndex == -1) {
                    tokenIndex = textLength
                }
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                //handle mismatch situation
                if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes.add(node)
                if (dotNode != null) nodes.add(dotNode)
            }
            if (!incomplete && nodes.isEmpty()) return null
            return ParadoxScopeFieldExpression(expressionString, range, nodes, configGroup)
        }

        private fun ParadoxScopeFieldExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            val context = ParadoxComplexExpressionProcessContext()
            val result = processAllNodesToValidate(errors, context) {
                when {
                    it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                    else -> true
                }
            }
            val malformed = !result
            if (malformed) errors += ParadoxComplexExpressionErrors.malformedScopeFieldExpression(rangeInExpression, text)
            return errors
        }
    }
}
