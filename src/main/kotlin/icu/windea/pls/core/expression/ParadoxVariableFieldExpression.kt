package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.ParadoxVariableFieldExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

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
    val scopeNodes: List<ParadoxScopeFieldExpressionNode>
    
    val variableNode: ParadoxDataExpressionNode
    
    companion object Resolver
}

class ParadoxVariableFieldExpressionImpl(
    override val text: String,
    override val isKey: Boolean?,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode>,
    override val configGroup: CwtConfigGroup
) : AbstractExpression(text), ParadoxVariableFieldExpression {
    override val quoted: Boolean = false
    
    override val scopeNodes: List<ParadoxScopeFieldExpressionNode> = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()
    
    override val variableNode: ParadoxDataExpressionNode = nodes.last().cast()
    
    override fun validate(): List<ParadoxExpressionError> {
        val errors = SmartList<ParadoxExpressionError>()
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
        return node.text.isExactParameterizedIdentifier()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val startOffset = context.startOffset
        val isKey = context.isKey
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        
        context.isKey = null
        
        var scopeContextInExpression = scopeContext
        val offsetInParent = context.offsetInParent
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
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(node, scopeContextInExpression, inExpression)
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
        context.startOffset = startOffset
        context.isKey = isKey
        context.scopeContext = scopeContext
    }
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxVariableFieldExpression? {
    //skip if text represents an int or float
    val type = ParadoxDataExpression.resolve(text).type
    if(type == ParadoxType.IntType || type == ParadoxType.FloatType) return null
    
    val nodes = SmartList<ParadoxExpressionNode>()
    val offset = textRange.startOffset
    var isLast = false
    var index: Int
    var dotIndex = -1
    while(dotIndex < text.length) {
        index = dotIndex + 1
        dotIndex = text.indexOf('.', index)
        if(text.indexOf('@', index).let { it != -1 && dotIndex > it }) {
            dotIndex = -1
        }
        if(text.indexOf('|', index).let { it != -1 && dotIndex > it }) {
            dotIndex = -1
        }
        val dotNode = if(dotIndex != -1) {
            val dotRange = TextRange.create(dotIndex + offset, dotIndex + 1 + offset)
            ParadoxOperatorExpressionNode(".", dotRange)
        } else {
            null
        }
        if(dotIndex == -1) {
            dotIndex = text.length
            isLast = true
        }
        //resolve node
        val nodeText = text.substring(index, dotIndex)
        val nodeTextRange = TextRange.create(index + offset, dotIndex + offset)
        val node = when {
            isLast -> ParadoxDataExpressionNode.resolve(nodeText, nodeTextRange, configGroup.linksAsVariable)
            else -> ParadoxScopeFieldExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
        }
        //handle mismatch situation
        if(!canBeMismatched && index == 0 && node is ParadoxErrorExpressionNode) {
            return null
        }
        nodes.add(node)
        if(dotNode != null) nodes.add(dotNode)
    }
    return ParadoxVariableFieldExpressionImpl(text, isKey, textRange, nodes, configGroup)
}