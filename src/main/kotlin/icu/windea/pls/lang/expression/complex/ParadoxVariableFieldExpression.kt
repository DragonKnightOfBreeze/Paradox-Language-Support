package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
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

    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val context = ParadoxComplexExpressionProcessContext()
        val result = processAllNodesToValidate(errors, context) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedVariableFieldExpression(rangeInExpression, text)
        return errors
    }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
            val incomplete = PlsManager.incompleteComplexExpression.get() ?: false
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
            return ParadoxScriptExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }
    }
}
