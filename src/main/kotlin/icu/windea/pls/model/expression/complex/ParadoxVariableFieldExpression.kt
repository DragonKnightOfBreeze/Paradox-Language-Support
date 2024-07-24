package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.nodes.*

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
    val scopeNodes: List<ParadoxScopeFieldNode>
        get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()
    val variableNode: ParadoxDataSourceNode
        get() = nodes.last().cast()
    
    override val errors by lazy { validate() }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        val isKey = context.isKey
        
        context.isKey = null
        
        val offset = context.offsetInParent!! - context.expressionOffset
        if(offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for((i, node) in nodes.withIndex()) {
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
                    val inExpression = i != 0
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(contextElement, node, scopeContextInExpression, inExpression)
                }
            } else if(node is ParadoxDataSourceNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeFieldNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeNode(scopeNode, context, result)
                    if(afterPrefix) break
                    completeForVariableDataSourceNode(node, context, result)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
            if(expressionString.isEmpty()) return null
            
            //skip if text is a number
            if(isNumber(expressionString)) return null
            
            val parameterRanges = expressionString.getParameterRanges()
            
            //skip if text is a parameter with unary operator prefix
            if(ParadoxExpressionHandler.isUnaryOperatorAwareParameter(expressionString, parameterRanges)) return null
            
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val expression = ParadoxVariableFieldExpression(expressionString, range, nodes, configGroup)
            val offset = range.startOffset
            var isLast = false
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val textLength = expressionString.length
            while(tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString.indexOf('.', index)
                if(tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //这里需要跳过参数文本
                if(tokenIndex != -1 && expressionString.indexOf('@', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                if(tokenIndex != -1 && expressionString.indexOf('|', index).let { i -> i != -1 && i < tokenIndex && !parameterRanges.any { r -> i in r } }) tokenIndex = -1
                val dotNode = if(tokenIndex != -1) {
                    val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxOperatorNode(".", dotRange)
                } else {
                    null
                }
                if(tokenIndex == -1) {
                    tokenIndex = textLength
                    isLast = true
                }
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = when {
                    isLast -> ParadoxDataSourceNode.resolve(nodeText, nodeTextRange, configGroup.linksAsVariable)
                    else -> ParadoxScopeFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                }
                //handle mismatch situation
                if(!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes.add(node)
                if(dotNode != null) nodes.add(dotNode)
            }
            return expression
        }
        
        private fun isNumber(text: String): Boolean {
            return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }
        
        private fun ParadoxVariableFieldExpression.validate(): List<ParadoxComplexExpressionError> {
            val errors = mutableListOf<ParadoxComplexExpressionError>()
            var malformed = false
            for((i, node) in nodes.withIndex()) {
                val isLast = i == nodes.lastIndex
                when(node) {
                    is ParadoxScopeFieldNode -> {
                        if(node.text.isEmpty()) {
                            if(!malformed) {
                                malformed = true
                            }
                        } else {
                            if(node is ParadoxScopeLinkFromDataNode) {
                                val dataSourceNode = node.dataSourceNode
                                for(dataSourceChildNode in dataSourceNode.nodes) {
                                    when(dataSourceChildNode) {
                                        is ParadoxDataSourceNode -> {
                                            if(dataSourceChildNode.text.isEmpty()) {
                                                if(!malformed) {
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
                    is ParadoxDataSourceNode -> {
                        if(node.text.isEmpty()) {
                            if(isLast) {
                                errors += ParadoxComplexExpressionErrors.missingVariable(rangeInExpression)
                            } else if(!malformed) {
                                malformed = true
                            }
                        } else if(!malformed && !node.isValid()) {
                            malformed = true
                        }
                    }
                }
            }
            if(malformed) {
                errors += ParadoxComplexExpressionErrors.malformedVariableFieldExpression(rangeInExpression, text)
            }
            return errors.pinned { it.isMalformedError() }
        }
        
        private fun ParadoxComplexExpressionNode.isValid(): Boolean {
            return text.isParameterAwareIdentifier()
        }
    }
}
