package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

/**
 * 作用域字段表达式。对应的CWT规则类型为[CwtDataTypeGroups.ScopeField]。
 *
 * 语法：
 *
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_link | scope_link | scope_link_from_data
 * system_link ::= TOKEN //predefined by CWT Config (see system_links.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= EXPRESSION //e.g. "some_variable" while the link's data source is "value[variable]"
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
    val scopeNodes: List<ParadoxScopeFieldNode>
        get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()
    
    override val errors by lazy { validate() }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement?.castOrNull<ParadoxScriptExpressionElement>() ?: return
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        val isKey = context.isKey
        
        context.isKey = null
        
        val offset = context.offsetInParent!! - context.expressionOffset
        if(offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for(node in nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if(!inRange) {
                //如果光标位置之前存在无法解析的scope（除非被解析为scopeLinkFromData，例如，"event_target:xxx"），不要进行补全
                if(node is ParadoxErrorNode || node.text.isEmpty()) break
            }
            if(node is ParadoxScopeFieldNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForScopeNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeHandler.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                        ?: ParadoxScopeHandler.getUnknownScopeContext(scopeContextInExpression)
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? {
            if(expressionString.isEmpty()) return null
            
            val parameterRanges = expressionString.getParameterRanges()
            
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = range.startOffset
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val textLength = expressionString.length
            while(tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString.indexOf('.', index)
                if(tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                if(tokenIndex != -1 && expressionString.indexOf('@', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                if(tokenIndex != -1 && expressionString.indexOf('|', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                val dotNode = if(tokenIndex != -1) {
                    val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxOperatorNode(".", dotRange, configGroup)
                } else {
                    null
                }
                if(tokenIndex == -1) {
                    tokenIndex = textLength
                }
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = ParadoxScopeFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                //handle mismatch situation
                if(!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes.add(node)
                if(dotNode != null) nodes.add(dotNode)
            }
            return ParadoxScopeFieldExpression(expressionString, range, nodes, configGroup)
        }
        
        private fun ParadoxScopeFieldExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            var malformed = false
            for((i, node) in nodes.withIndex()) {
                val isLast = i == nodes.lastIndex
                when(node) {
                    is ParadoxScopeFieldNode -> {
                        if(node.text.isEmpty()) {
                            if(isLast) {
                                errors += ParadoxComplexExpressionErrors.missingScopeField(rangeInExpression)
                            } else if(!malformed) {
                                malformed = true
                            }
                        } else {
                            if(node is ParadoxScopeLinkFromDataNode) {
                                val dataSourceNode = node.dataSourceNode
                                for(dataSourceChildNode in dataSourceNode.nodes) {
                                    when(dataSourceChildNode) {
                                        is ParadoxDataSourceNode -> {
                                            if(dataSourceChildNode.text.isEmpty()) {
                                                if(isLast) {
                                                    val expect = dataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                    errors += ParadoxComplexExpressionErrors.missingScopeLinkDataSource(rangeInExpression, expect)
                                                } else if(!malformed) {
                                                    malformed = true
                                                }
                                            } else if(!malformed && !dataSourceChildNode.isValid()) {
                                                malformed = true
                                            }
                                        }
                                        is ParadoxScopeFieldNode -> {
                                            if(dataSourceChildNode.text.isEmpty()) {
                                                if(isLast) {
                                                    errors += ParadoxComplexExpressionErrors.missingScopeField(rangeInExpression)
                                                } else if(!malformed) {
                                                    malformed = true
                                                }
                                            } else {
                                                if(dataSourceChildNode is ParadoxScopeLinkFromDataNode) {
                                                    val nestedDataSourceNode = dataSourceChildNode.dataSourceNode
                                                    for(nestedDataSourceChildNode in nestedDataSourceNode.nodes) {
                                                        when(nestedDataSourceChildNode) {
                                                            is ParadoxDataSourceNode -> {
                                                                if(nestedDataSourceChildNode.text.isEmpty()) {
                                                                    if(isLast) {
                                                                        val expect = nestedDataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                                        errors += ParadoxComplexExpressionErrors.missingScopeLinkDataSource(rangeInExpression, expect)
                                                                    } else if(!malformed) {
                                                                        malformed = true
                                                                    }
                                                                } else if(!malformed && !nestedDataSourceChildNode.isValid()) {
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
                }
            }
            if(malformed) {
                errors += ParadoxComplexExpressionErrors.malformedScopeFieldExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }
        
        private fun ParadoxComplexExpressionNode.isValid(): Boolean {
            return text.isParameterAwareIdentifier()
        }
    }
}
