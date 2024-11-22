package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
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
        get() = this.valueFieldNode.castOrNull<ParadoxDynamicValueFieldNode>()
            ?.dataSourceNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

    override val errors by lazy { validate() }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
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
                nodes.add(node)
                if (dotNode != null) nodes.add(dotNode)
            }
            if (!incomplete && nodes.isEmpty()) return null
            return ParadoxValueFieldExpression(expressionString, range, nodes, configGroup)
        }

        private fun isNumber(text: String): Boolean {
            return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }

        private fun ParadoxValueFieldExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            var malformed = false
            for ((i, node) in nodes.withIndex()) {
                val isLast = i == nodes.lastIndex
                when (node) {
                    is ParadoxScopeLinkNode -> {
                        if (node.text.isEmpty()) {
                            if (!malformed) {
                                malformed = true
                            }
                        } else {
                            if (node is ParadoxDynamicScopeLinkNode) {
                                val dataSourceNode = node.dataSourceNode
                                for (dataSourceChildNode in dataSourceNode.nodes) {
                                    when (dataSourceChildNode) {
                                        is ParadoxDataSourceNode -> {
                                            if (dataSourceChildNode.text.isEmpty()) {
                                                if (!malformed) {
                                                    malformed = true
                                                }
                                            } else if (!malformed && !dataSourceChildNode.isValid()) {
                                                malformed = true
                                            }
                                        }
                                        is ParadoxScopeLinkNode -> {
                                            if (dataSourceChildNode.text.isEmpty()) {
                                                if (isLast) {
                                                    errors += ParadoxComplexExpressionErrors.missingScopeLink(rangeInExpression)
                                                } else if (!malformed) {
                                                    malformed = true
                                                }
                                            } else {
                                                if (dataSourceChildNode is ParadoxDynamicScopeLinkNode) {
                                                    val nestedDataSourceNode = dataSourceChildNode.dataSourceNode
                                                    for (nestedDataSourceChildNode in nestedDataSourceNode.nodes) {
                                                        when (nestedDataSourceChildNode) {
                                                            is ParadoxDataSourceNode -> {
                                                                if (nestedDataSourceChildNode.text.isEmpty()) {
                                                                    if (isLast) {
                                                                        val expect = nestedDataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                                        errors += ParadoxComplexExpressionErrors.missingScopeLinkValue(rangeInExpression, expect)
                                                                    } else if (!malformed) {
                                                                        malformed = true
                                                                    }
                                                                } else if (!malformed && !nestedDataSourceChildNode.isValid()) {
                                                                    malformed = true
                                                                }
                                                            }
                                                            is ParadoxComplexExpression -> {
                                                                errors += nestedDataSourceChildNode.errors
                                                            }
                                                            is ParadoxErrorTokenNode -> {
                                                                malformed = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        is ParadoxComplexExpression -> {
                                            errors += dataSourceChildNode.errors
                                        }
                                        is ParadoxErrorTokenNode -> {
                                            malformed = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is ParadoxValueFieldNode -> {
                        if (node.text.isEmpty()) {
                            if (isLast) {
                                errors += ParadoxComplexExpressionErrors.missingValueField(rangeInExpression)
                            } else if (!malformed) {
                                malformed = true
                            }
                        } else {
                            if (node is ParadoxDynamicValueFieldNode) {
                                val dataSourceNode = node.dataSourceNode
                                for (dataSourceChildNode in dataSourceNode.nodes) {
                                    when (dataSourceChildNode) {
                                        is ParadoxDataSourceNode -> {
                                            if (dataSourceChildNode.text.isEmpty()) {
                                                if (isLast) {
                                                    val expect = dataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                    errors += ParadoxComplexExpressionErrors.missingValueFieldValue(rangeInExpression, expect)
                                                } else if (!malformed) {
                                                    malformed = true
                                                }
                                            } else if (!malformed && !dataSourceChildNode.isValid()) {
                                                malformed = true
                                            }
                                        }
                                        is ParadoxComplexExpression -> {
                                            errors += dataSourceChildNode.errors
                                        }
                                        is ParadoxErrorTokenNode -> {
                                            malformed = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (malformed) {
                errors += ParadoxComplexExpressionErrors.malformedValueFieldExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }

        private fun ParadoxComplexExpressionNode.isValid(): Boolean {
            return text.isParameterAwareIdentifier()
        }
    }
}
