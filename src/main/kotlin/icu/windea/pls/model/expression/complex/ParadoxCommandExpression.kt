@file:Suppress("KDocUnresolvedReference")

package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.expression.complex.nodes.*
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.*

/**
 * （本地化）命令表达式。
 *
 * 可以在本地化文件中作为命令文本使用。（如，`[Root.GetName]`）
 *
 * 语法：
 *
 * ```bnf
 * command_expression ::= command_scope_link * (command_field)
 * command_scope_link := system_command_scope | command_scope | command_dynamic_scope_link
 * system_command_scope := TOKEN //predefined by CWT Config (see system links)
 * command_scope := TOKEN //predefined by CWT Config (see localisation links)
 * command_dynamic_scope_link := command_dynamic_scope_link_prefix ? command_dynamic_scope_link_value
 * command_dynamic_scope_link_prefix := TOKEN //"event_target:"
 * command_dynamic_scope_link_value := TOKEN //matching config expression "value[event_target]" or "value[global_event_target]"
 * command_field ::= command_predefined_field | command_dynamic_field
 * command_predefined_field := TOKEN //predefined by CWT Config (see localisation commands)
 * command_dynamic_field ::= TOKEN //matching config expression "<scripted_loc>" or "value[variable]"
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
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //TODO 1.3.15+
    }
    
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
