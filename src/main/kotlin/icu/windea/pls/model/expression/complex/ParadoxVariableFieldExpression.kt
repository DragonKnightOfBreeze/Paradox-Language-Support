package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.ParadoxDataExpression.*

/**
 * 变量字段表达式。
 *
 * 相较于值字段表达式（[ParadoxValueFieldExpression]），仅支持调用变量（可带上作用域信息）。
 *
 * 示例：
 *
 * ```
 * root.owner.some_variable
 * ```
 */
interface ParadoxVariableFieldExpression : ParadoxComplexExpression {
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? =
            doResolve(expression, range, configGroup)
    }
}

val ParadoxVariableFieldExpression.scopeNodes: List<ParadoxScopeFieldExpressionNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()
val ParadoxVariableFieldExpression.variableNode: ParadoxDataExpressionNode
    get() = nodes.last().cast()

//Implementations

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxVariableFieldExpression? {
    //skip if text represents an int or float
    if(isNumber(expression)) return null
    
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expression)
    //skip if text is a parameter with unary operator prefix
    if(CwtConfigHandler.isUnaryOperatorAwareParameter(expression, parameterRanges)) return null
    
    val incomplete = PlsContext.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxExpressionNode>()
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
            ParadoxOperatorExpressionNode(".", dotRange)
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
            isLast -> ParadoxDataExpressionNode.resolve(nodeText, nodeTextRange, configGroup.linksAsVariable)
            else -> ParadoxScopeFieldExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
        }
        //handle mismatch situation
        if(!incomplete && nodes.isEmpty() && node is ParadoxErrorExpressionNode) return null
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
    override val nodes: List<ParadoxExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxVariableFieldExpression {
    override fun validate(): List<ParadoxExpressionError> {
        val errors = mutableListOf<ParadoxExpressionError>()
        var malformed = false
        for((index, node) in nodes.withIndex()) {
            val isLast = index == nodes.lastIndex
            when(node) {
                is ParadoxScopeFieldExpressionNode -> {
                    if(node.text.isEmpty()) {
                        if(!malformed) {
                            malformed = true
                        }
                    } else {
                        if(node is ParadoxScopeLinkFromDataExpressionNode) {
                            val dataSourceNode = node.dataSourceNode
                            for(dataSourceChildNode in dataSourceNode.nodes) {
                                when(dataSourceChildNode) {
                                    is ParadoxDataExpressionNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(!malformed) {
                                                malformed = true
                                            }
                                        } else if(!malformed && !isValid(dataSourceChildNode)) {
                                            malformed = true
                                        }
                                    }
                                    is ParadoxScopeFieldExpressionNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val error = ParadoxMissingScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingScope"))
                                                errors.add(error)
                                            } else if(!malformed) {
                                                malformed = true
                                            }
                                        } else {
                                            if(dataSourceChildNode is ParadoxScopeLinkFromDataExpressionNode) {
                                                val nestedDataSourceNode = dataSourceChildNode.dataSourceNode
                                                for(nestedDataSourceChildNode in nestedDataSourceNode.nodes) {
                                                    when(nestedDataSourceChildNode) {
                                                        is ParadoxDataExpressionNode -> {
                                                            if(nestedDataSourceChildNode.text.isEmpty()) {
                                                                if(isLast) {
                                                                    val expect = nestedDataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                                    val error = ParadoxMissingScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingScopeLinkDataSource", expect))
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
                                                        is ParadoxErrorTokenExpressionNode -> {
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
                                    is ParadoxErrorTokenExpressionNode -> {
                                        malformed = true
                                    }
                                }
                            }
                        }
                    }
                }
                is ParadoxDataExpressionNode -> {
                    if(node.text.isEmpty()) {
                        if(isLast) {
                            val error = ParadoxMissingVariableExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingVariable"))
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
            val error = ParadoxMalformedVariableFieldExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedVariableFieldExpression", text))
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxExpressionNode): Boolean {
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
                if(node is ParadoxErrorExpressionNode || node.text.isEmpty()) break
            }
            if(node is ParadoxScopeFieldExpressionNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForScopeExpressionNode(node, context, result)
                    break
                } else {
                    val inExpression = i != 0
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(contextElement, node, scopeContextInExpression, inExpression)
                }
            } else if(node is ParadoxDataExpressionNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    val scopeExpressionNode = ParadoxScopeFieldExpressionNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeExpressionNode(scopeExpressionNode, context, result)
                    if(afterPrefix) break
                    completeForVariableDataExpressionNode(node, context, result)
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