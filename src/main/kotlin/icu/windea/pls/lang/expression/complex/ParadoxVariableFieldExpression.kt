package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * 变量字段表达式。作为[ParadoxValueFieldExpression]的子集。
 * 相较之下，仅支持调用变量（可带上作用域信息）。
 *
 * 示例：
 *
 * `root.owner.some_variable`
 */
class ParadoxVariableFieldExpression(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    val scopeNodes: List<ParadoxScopeLinkNode>
        get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
    val variableNode: ParadoxDataSourceNode
        get() = nodes.last().cast()

    override val errors by lazy { validate() }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            //skip if text is a number
            if (isNumber(expressionString)) return null

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

            //skip if text is a parameter with unary operator prefix
            if (ParadoxExpressionManager.isUnaryOperatorAwareParameter(expressionString, parameterRanges)) return null
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxVariableFieldExpression(expressionString, range, nodes, configGroup)
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
                    isLast -> ParadoxDataSourceNode.resolve(nodeText, nodeTextRange, configGroup, configGroup.linksOfVariable)
                    else -> ParadoxScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                }
                //handle mismatch situation
                if (!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes.add(node)
                if (dotNode != null) nodes.add(dotNode)
            }
            if (!incomplete && nodes.isEmpty()) return null
            return expression
        }

        private fun isNumber(text: String): Boolean {
            return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }

        private fun ParadoxVariableFieldExpression.validate(): List<ParadoxComplexExpressionError> {
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
                    is ParadoxDataSourceNode -> {
                        if (node.text.isEmpty()) {
                            if (isLast) {
                                errors += ParadoxComplexExpressionErrors.missingVariable(rangeInExpression)
                            } else if (!malformed) {
                                malformed = true
                            }
                        } else if (!malformed && !node.isValid()) {
                            malformed = true
                        }
                    }
                }
            }
            if (malformed) {
                errors += ParadoxComplexExpressionErrors.malformedVariableFieldExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }

        private fun ParadoxComplexExpressionNode.isValid(): Boolean {
            return text.isParameterAwareIdentifier()
        }
    }
}
