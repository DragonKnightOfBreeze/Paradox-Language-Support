@file:Suppress("KDocUnresolvedReference")

package icu.windea.pls.lang.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.nodes.*

/**
 * （本地化）命令表达式。
 *
 * 可以在本地化文件中作为命令文本使用。（如，`[Root.GetName]`）
 *
 * 语法：
 *
 * ```bnf
 * command_expression ::= command_scope_link * (command_field) tagging_suffix ?
 * command_scope_link := system_command_scope | command_scope | dynamic_command_scope_link
 * system_command_scope := TOKEN //predefined by CWT Config (see system scopes)
 * command_scope := TOKEN //predefined by CWT Config (see localisation links)
 * dynamic_command_scope_link := dynamic_command_scope_link_prefix ? dynamic_command_scope_link_value
 * dynamic_command_scope_link_prefix := TOKEN //"event_target:"
 * dynamic_command_scope_link_value := TOKEN //matching config expression "value[event_target]" or "value[global_event_target]"
 * command_field ::= predefined_command_field | dynamic_command_field
 * predefined_command_field := TOKEN //predefined by CWT Config (see localisation commands)
 * dynamic_command_field ::= TOKEN //matching config expression "<scripted_loc>" or "value[variable]"
 * tagging_suffix ::= TOKEN //see 99_README_GRAMMAR.txt
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
    override val errors by lazy { validate() }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxCommandExpression? {
            if(expressionString.isEmpty()) return null
            
            //val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            
            val parameterRanges = expressionString.getParameterRanges()
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxCommandExpression(expressionString, range, nodes, configGroup)
            run r1@{
                val offset = range.startOffset
                var index: Int
                var tokenIndex = -1
                var startIndex = 0
                val textLength = expressionString.length
                while(tokenIndex < textLength) {
                    index = tokenIndex + 1
                    tokenIndex = expressionString.indexOf('.', index)
                    if(tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                    if(tokenIndex == -1) tokenIndex = textLength
                    run r2@{
                        val nodeText = expressionString.substring(startIndex, tokenIndex)
                        val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                        startIndex = tokenIndex + 1
                        val node = when {
                            tokenIndex != textLength -> ParadoxCommandScopeLinkNode.resolve(nodeText, nodeTextRange, configGroup)
                            else -> ParadoxCommandFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                        }
                        nodes += node
                    }
                    run r2@{
                        if(tokenIndex == textLength) return@r2
                        val node = ParadoxOperatorNode(".", TextRange.from(tokenIndex, 1), configGroup)
                        nodes += node
                    }
                }
            }
            return expression
        }
        
        private fun ParadoxCommandExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            var malformed = false
            for(node in nodes) {
                if(node.text.isEmpty()) {
                    malformed = true
                    break
                }
            }
            if(malformed) {
                errors += ParadoxComplexExpressionErrors.malformedLocalisationCommandExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }
    }
}
