package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
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
interface ParadoxVariableFieldExpression : ParadoxComplexExpression {
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? =
            doResolve(expression, range, configGroup)
    }
}

//Implementations

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
    //skip if text represents an int or float
    if(isNumber(expression)) return null
    
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expression)
    //skip if text is a parameter with unary operator prefix
    if(CwtConfigHandler.isUnaryOperatorAwareParameter(expression, parameterRanges)) return null
    
    val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxComplexExpressionNode>()
    val offset = range.startOffset
    var isLast = false
    var index: Int
    var tokenIndex = -1
    var startIndex = 0
    val textLength = expression.length
    while(tokenIndex < textLength) {
        index = tokenIndex + 1
        tokenIndex = expression.indexOf('.', index)
        if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
        if(tokenIndex != -1 && expression.indexOf('@', index).let { it != -1 && it < tokenIndex && !CwtConfigHandler.inParameterRanges(parameterRanges, it) }) tokenIndex = -1
        if(tokenIndex != -1 && expression.indexOf('|', index).let { it != -1 && it < tokenIndex && !CwtConfigHandler.inParameterRanges(parameterRanges, it) }) tokenIndex = -1
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
        val nodeText = expression.substring(startIndex, tokenIndex)
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
    return ParadoxVariableFieldExpressionImpl(expression, range, nodes, configGroup)
}

private fun isNumber(text: String): Boolean {
    return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
}

private class ParadoxVariableFieldExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxVariableFieldExpression {
    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var malformed = false
        for((index, node) in nodes.withIndex()) {
            val isLast = index == nodes.lastIndex
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
                is ParadoxDataSourceNode -> {
                    if(node.text.isEmpty()) {
                        if(isLast) {
                            val error = ParadoxComplexExpressionErrors.missingVariable(rangeInExpression)
                            errors.add(error)
                        } else if(!malformed) {
                            malformed = true
                        }
                    } else if(!malformed && !isValid(node)) {
                        malformed = true
                    }
                }
            }
        }
        if(malformed) {
            val error = ParadoxComplexExpressionErrors.malformedVariableFieldExpression(rangeInExpression, text)
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
        context.isKey = isKey
        context.scopeContext = scopeContext
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxVariableFieldExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}