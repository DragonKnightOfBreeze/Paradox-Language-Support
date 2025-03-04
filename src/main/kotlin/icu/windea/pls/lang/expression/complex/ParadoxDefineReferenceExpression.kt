package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*

/**
 * 定义引用表达式。对应的CWT规则类型为[CwtDataTypes.DefineReference]。
 *
 * 语法：
 *
 * ```bnf
 * define_reference_expression ::= "define:" define_namespace "|" define_variable
 * define_namespace ::= TOKEN //level 1 property keys in .txt files in common/defines
 * define_variable ::= TOKEN //level 2 property keys in .txt files in common/defines
 * ```
 *
 * 示例：
 *
 * * `define:NPortrait|GRACEFUL_AGING_START`
 */
class ParadoxDefineReferenceExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    override val errors by lazy { validate() }

    val namespaceNode: ParadoxDefineNamespaceNode?
        get() = nodes.getOrNull(1)?.cast()
    val variableNode: ParadoxDefineVariableNode?
        get() = nodes.getOrNull(3)?.cast()

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxDefineReferenceExpression? {
            val incomplete = PlsManager.incompleteComplexExpression.get() ?: false
            if (!incomplete && expressionString.isEmpty()) return null

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxDefineReferenceExpression(expressionString, range, nodes, configGroup)
            run r1@{
                val offset = range.startOffset
                val prefix = "define:"
                if (expressionString.startsWith(prefix)) {
                    val node = ParadoxDefinePrefixNode(prefix, TextRange.from(offset, prefix.length), configGroup)
                    nodes += node
                } else {
                    if (!incomplete) return null
                    val nodeTextRange = TextRange.from(offset, expressionString.length)
                    val node = ParadoxErrorTokenNode(expressionString, nodeTextRange, configGroup)
                    nodes += node
                    return@r1
                }
                val pipeIndex = expressionString.indexOf('|', prefix.length)
                run r2@{
                    val nodeText = if (pipeIndex == -1) expressionString.substring(prefix.length) else expressionString.substring(prefix.length, pipeIndex)
                    val nodeTextRange = TextRange.from(offset + prefix.length, nodeText.length)
                    val node = ParadoxDefineNamespaceNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                    nodes += node
                }
                if (pipeIndex == -1) return@r1
                run r2@{
                    val nodeTextRange = TextRange.from(offset + pipeIndex, 1)
                    val node = ParadoxMarkerNode("|", nodeTextRange, configGroup)
                    nodes += node
                }
                run r2@{
                    val nodeText = expressionString.substring(pipeIndex + 1)
                    val nodeTextRange = TextRange.from(offset + pipeIndex + 1, nodeText.length)
                    val node = ParadoxDefineVariableNode.resolve(nodeText, nodeTextRange, configGroup, expression)
                    nodes += node
                }
            }
            if (!incomplete && nodes.isEmpty()) return null
            return expression
        }

        private fun ParadoxDefineReferenceExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            val context = ParadoxComplexExpressionProcessContext()
            val result = processAllNodesToValidate(errors, context) {
                when {
                    it is ParadoxDefineNamespaceNode -> it.text.isParameterAwareIdentifier()
                    it is ParadoxDefineVariableNode -> it.text.isParameterAwareIdentifier()
                    else -> true
                }
            }
            val malformed = !result || nodes.size != 4
            if (malformed) errors += ParadoxComplexExpressionErrors.malformedDefineReferenceExpression(rangeInExpression, text)
            return errors
        }
    }
}
