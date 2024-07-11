package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.nodes.*

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
interface ParadoxScopeFieldExpression : ParadoxComplexExpression {
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? =
            doResolve(expressionString, range, configGroup)
    }
}

//Implementations

private fun doResolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxScopeFieldExpression? {
    if(expressionString.isEmpty()) return null
    
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expressionString)
    
    val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxComplexExpressionNode>()
    val offset = range.startOffset
    var index: Int
    var tokenIndex = -1
    var startIndex = 0
    val textLength = expressionString.length
    while(tokenIndex < textLength) {
        index = tokenIndex + 1
        tokenIndex = expressionString.indexOf('.', index)
        if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
        if(tokenIndex != -1 && expressionString.indexOf('@', index).let { it != -1 && it < tokenIndex && !CwtConfigHandler.inParameterRanges(parameterRanges, it) }) tokenIndex = -1
        if(tokenIndex != -1 && expressionString.indexOf('|', index).let { it != -1 && it < tokenIndex && !CwtConfigHandler.inParameterRanges(parameterRanges, it) }) tokenIndex = -1
        val dotNode = if(tokenIndex != -1) {
            val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
            ParadoxOperatorNode(".", dotRange)
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
    return ParadoxScopeFieldExpressionImpl(expressionString, range, nodes, configGroup)
}

private class ParadoxScopeFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxScopeFieldExpression {
    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var malformed = false
        for((i, node) in nodes.withIndex()) {
            val isLast = i == nodes.lastIndex
            when(node) {
                is ParadoxScopeFieldNode -> {
                    if(node.text.isEmpty()) {
                        if(isLast) {
                            val error = ParadoxComplexExpressionErrors.missingScopeField(rangeInExpression)
                            errors.add(error)
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
                                                val error = ParadoxComplexExpressionErrors.missingScopeLinkDataSource(rangeInExpression, expect)
                                                errors.add(error)
                                            } else if(!malformed) {
                                                malformed = true
                                            }
                                        } else if(!malformed && !isValid(dataSourceChildNode)) {
                                            malformed = true
                                        }
                                    }
                                    is ParadoxScopeFieldNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val error = ParadoxComplexExpressionErrors.missingScopeField(rangeInExpression)
                                                errors.add(error)
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
                                                                    val error = ParadoxComplexExpressionErrors.missingScopeLinkDataSource(rangeInExpression, expect)
                                                                    errors.add(error)
                                                                } else if(!malformed) {
                                                                    malformed = true
                                                                }
                                                            } else if(!malformed && !isValid(nestedDataSourceChildNode)) {
                                                                malformed = true
                                                            }
                                                        }
                                                        is ParadoxComplexExpression -> {
                                                            errors.addAll(nestedDataSourceChildNode.validate())
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
                                        errors.addAll(dataSourceChildNode.validate())
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
            val error = ParadoxComplexExpressionErrors.malformedScopeFieldExpression(rangeInExpression, text)
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxComplexExpressionNode): Boolean {
        return node.text.isExactParameterAwareIdentifier()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val offsetInParent = context.offsetInParent!!
        val isKey = context.isKey
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        
        context.isKey = null
        
        var scopeContextInExpression = scopeContext
        for((i, node) in nodes.withIndex()) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
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
                    val inExpression = i != 0
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(contextElement, node, scopeContextInExpression, inExpression)
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.isKey = isKey
        context.scopeContext = scopeContext
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxScopeFieldExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}
