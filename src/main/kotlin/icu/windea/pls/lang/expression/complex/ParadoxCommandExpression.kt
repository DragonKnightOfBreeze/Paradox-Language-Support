@file:Suppress("KDocUnresolvedReference")

package icu.windea.pls.lang.expression.complex

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*

/**
 * （本地化）命令表达式。
 *
 * 可以在本地化文件中作为命令文本使用。（如，`[Root.GetName]`）
 *
 * 语法：
 *
 * ```bnf
 * command_expression ::= command_scope_link * (command_field) suffix ?
 * command_scope_link := system_command_scope | command_scope | dynamic_command_scope_link
 * system_command_scope := TOKEN //predefined by CWT Config (see system scopes)
 * command_scope := TOKEN //predefined by CWT Config (see localisation links)
 * dynamic_command_scope_link := dynamic_command_scope_link_prefix ? dynamic_command_scope_link_value
 * dynamic_command_scope_link_prefix := TOKEN //"event_target:", "parameter:", etc.
 * dynamic_command_scope_link_value := TOKEN //matching config expression "value[event_target]" or "value[global_event_target]"
 * command_field ::= predefined_command_field | dynamic_command_field
 * predefined_command_field := TOKEN //predefined by CWT Config (see localisation commands)
 * dynamic_command_field ::= TOKEN //matching config expression "<scripted_loc>" or "value[variable]"
 * suffix ::= TOKEN //see 99_README_GRAMMAR.txt
 * ```
 *
 * 示例：
 *
 * * `Root.GetName`
 * * `Root.Owner.event_target:some_event_target.var`
 */
class ParadoxCommandExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
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
        if (malformed) errors += ParadoxComplexExpressionErrors.malformedCommandExpression(rangeInExpression, text)
        return errors
    }

    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression? {
            if (expressionString.isEmpty()) return null

            //val incomplete = PlsStates.incompleteComplexExpression.get() ?: false

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(expressionString)

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxCommandExpression(expressionString, range, nodes, configGroup)
            val suffixNodes = mutableListOf<ParadoxComplexExpressionNode>()
            var suffixStartIndex: Int
            run r1@{
                run r2@{
                    suffixStartIndex = expressionString.indexOf('&')
                    if (suffixStartIndex == -1) return@r2
                    run r3@{
                        val node = ParadoxMarkerNode("&", TextRange.from(suffixStartIndex, 1), configGroup)
                        suffixNodes += node
                    }
                    run r3@{
                        val nodeText = expressionString.substring(suffixStartIndex + 1)
                        val node = ParadoxCommandSuffixNode.resolve(nodeText, TextRange.from(suffixStartIndex + 1, nodeText.length), configGroup)
                        suffixNodes += node
                    }
                    return@r1
                }
                run r2@{
                    suffixStartIndex = expressionString.indexOf("::")
                    if (suffixStartIndex == -1) return@r2
                    run r3@{
                        val node = ParadoxMarkerNode("::", TextRange.from(suffixStartIndex, 2), configGroup)
                        suffixNodes += node
                    }
                    run r3@{
                        val nodeText = expressionString.substring(suffixStartIndex + 2)
                        val node = ParadoxCommandSuffixNode.resolve(nodeText, TextRange.from(suffixStartIndex + 2, nodeText.length), configGroup)
                        suffixNodes += node
                    }
                }
            }
            run r1@{
                val offset = range.startOffset
                var index: Int
                var tokenIndex = -1
                var startIndex = 0
                val expressionString0 = if (suffixStartIndex == -1) expressionString else expressionString.substring(0, suffixStartIndex)
                val textLength = expressionString0.length
                while (tokenIndex < textLength) {
                    index = tokenIndex + 1
                    tokenIndex = expressionString0.indexOf('.', index)
                    if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                    if (tokenIndex == -1) tokenIndex = textLength
                    run r2@{
                        val nodeText = expressionString0.substring(startIndex, tokenIndex)
                        val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                        startIndex = tokenIndex + 1
                        val node = when {
                            tokenIndex != textLength -> ParadoxCommandScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                            else -> ParadoxCommandFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                        }
                        nodes += node
                    }
                    run r2@{
                        if (tokenIndex == textLength) return@r2
                        val node = ParadoxOperatorNode(".", TextRange.from(tokenIndex, 1), configGroup)
                        nodes += node
                    }
                }
            }
            nodes += suffixNodes
            return expression
        }
    }
}
