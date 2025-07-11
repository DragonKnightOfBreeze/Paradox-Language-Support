package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * 值字段表达式。对应的CWT规则类型为[CwtDataTypeGroups.ValueField]。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by CWT Config (see system_scopes.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_value //predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_value ::= expression //e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * value_link_from_data ::= value_field_prefix value_field_value //predefined by CWT Config (see links.cwt)
 * value_field_prefix ::= TOKEN //e.g. "value:" while the link's prefix is "value:"
 * value_field_value ::= expression //e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | dynamic_value_expression | sv_expression //see: ParadoxDataExpression, ParadoxDynamicValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? //e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 *
 * * `trigger:some_trigger`
 * * `value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|`
 * * `root.owner.some_variable`
 */
class ParadoxValueFieldExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
    val valueFieldNode: ParadoxValueFieldNode
        get() = nodes.last().cast()
    val scriptValueExpression: ParadoxScriptValueExpression?
        get() = valueFieldNode.castOrNull<ParadoxDynamicValueFieldNode>()?.valueNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionError.Builder.malformedValueFieldExpression(rangeInExpression, text)
        return errors
    }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
            val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            //skip if text is a number
            if (isNumber(expressionString)) return null

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

            //skip if text is a parameter with unary operator prefix
            if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(expressionString, parameterRanges)) return null

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = range.startOffset
            var isLast = false
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
                if (tokenIndex != -1 && expressionString.indexOf('(', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                val dotNode = if (tokenIndex != -1) {
                    val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxOperatorNode(".", dotRange, configGroup)
                } else {
                    null
                }
                if (tokenIndex == -1) {
                    tokenIndex = textLength
                    isLast = true
                }
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = when {
                    isLast -> ParadoxValueFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                    else -> ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                }
                //handle mismatch situation
                if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes += node
                if (dotNode != null) nodes += dotNode
            }
            if (!incomplete && nodes.isEmpty()) return null
            return ParadoxValueFieldExpression(expressionString, range, nodes, configGroup)
        }

        private fun isNumber(text: String): Boolean {
            return ParadoxScriptExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }
    }
}
