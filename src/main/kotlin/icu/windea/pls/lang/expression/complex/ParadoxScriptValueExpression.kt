package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*
import kotlin.Pair

/**
 * 脚本值表达式。作为[ParadoxValueFieldExpression]的一部分。
 *
 * 语法：
 *
 * ```bnf
 * script_value_expression ::= script_value ("|" (arg_name "|" arg_value "|")+)?
 * script_value ::= TOKEN //matching config expression "<script_value>"
 * arg_name ::= TOKEN //argument name, no surrounding "$"
 * arg_value ::= TOKEN //boolean, int, float or string
 * ```
 *
 * 示例：
 *
 * * `some_sv`
 * * `some_sv|PARAM|VALUE|`
 */
class ParadoxScriptValueExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val config: CwtConfig<*>
) : ParadoxComplexExpression.Base() {
    val scriptValueNode: ParadoxScriptValueNode
        get() = nodes.first().cast()
    val argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>
        get() = buildList {
            var argumentNode: ParadoxScriptValueArgumentNode? = null
            for (node in nodes) {
                if (node is ParadoxScriptValueArgumentNode) {
                    argumentNode = node
                } else if (node is ParadoxScriptValueArgumentValueNode && argumentNode != null) {
                    add(tupleOf(argumentNode, node))
                    argumentNode = null
                }
            }
            if (argumentNode != null) {
                add(tupleOf(argumentNode, null))
            }
        }

    override val errors by lazy { validate() }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression? {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = range.startOffset
            var n = 0
            var valueNode: ParadoxScriptValueNode? = null
            var argumentNode: ParadoxScriptValueArgumentNode? = null
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val textLength = expressionString.length
            while (tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString.indexOf('|', index)
                if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                val pipeNode = if (tokenIndex != -1) {
                    val pipeRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxMarkerNode("|", pipeRange, configGroup)
                } else {
                    null
                }
                if (tokenIndex == -1) {
                    tokenIndex = textLength
                }
                if (!incomplete && index == tokenIndex && tokenIndex == textLength) break
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = when {
                    n == 0 -> {
                        ParadoxScriptValueNode.resolve(nodeText, nodeRange, configGroup, config)
                            .also { valueNode = it }
                    }
                    n % 2 == 1 -> {
                        ParadoxScriptValueArgumentNode.resolve(nodeText, nodeRange, configGroup, valueNode)
                            .also { argumentNode = it }
                    }
                    n % 2 == 0 -> {
                        ParadoxScriptValueArgumentValueNode.resolve(nodeText, nodeRange, configGroup, valueNode, argumentNode)
                    }
                    else -> throw InternalError()
                }
                nodes.add(node)
                if (pipeNode != null) nodes.add(pipeNode)
                n++
            }
            if (!incomplete && nodes.isEmpty()) return null
            return ParadoxScriptValueExpression(expressionString, range, nodes, configGroup, config)
        }

        private fun ParadoxScriptValueExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            val context = ParadoxComplexExpressionProcessContext()
            val result = processAllNodesToValidate(errors, context) {
                when {
                    it is ParadoxScriptValueNode -> it.text.isParameterAwareIdentifier()
                    it is ParadoxScriptValueArgumentNode -> it.text.isIdentifier()
                    it is ParadoxScriptValueArgumentValueNode -> true
                    else -> true
                }
            }
            var malformed = !result
            if (!malformed) {
                //check whether pipe count is valid
                val pipeNodeCount = nodes.count { it is ParadoxTokenNode && it.text == "|" }
                if (pipeNodeCount == 1 || (pipeNodeCount != 0 && pipeNodeCount % 2 == 0)) {
                    malformed = true
                }
            }
            if (malformed) errors += ParadoxComplexExpressionErrors.malformedScriptValueExpression(rangeInExpression, text)
            return errors
        }
    }
}
