package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.ParadoxVariableFieldExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.highlighter.*

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
    val scopeNodes: List<ParadoxScopeExpressionNode>
    
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
    
    override val scopeNodes: List<ParadoxScopeExpressionNode> = nodes.filterIsInstance<ParadoxScopeExpressionNode>()
    
    override val variableNode: ParadoxDataExpressionNode = nodes.last().cast()
    
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.VARIABLE_FILED_EXPRESSION_KEY
    
    override fun validate(): List<ParadoxExpressionError> {
        val errors = SmartList<ParadoxExpressionError>()
        var malformed = false
        for((index, node) in nodes.withIndex()) {
            val isLast = index == nodes.lastIndex
            when(node) {
                is ParadoxScopeExpressionNode -> {
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
        return node.text.all { it.isExactIdentifierChar() }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val isKey = context.isKey
        val scopeContext = context.scopeContext
        
        context.put(PlsCompletionKeys.isKeyKey, null)
        
        var scopeContextInExpression = scopeContext
        val offsetInParent = context.offsetInParent
        for(node in nodes) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
            if(!inRange) {
                //如果光标位置之前存在无法解析的scope（除非被解析为scopeLinkFromData，例如，"event_target:xxx"），不要进行补全
                if(node is ParadoxErrorExpressionNode || node.text.isEmpty()) break
            }
            if(node is ParadoxScopeExpressionNode) {
                if(inRange) {
                    val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
                    val prefixNode = linkFromDataNode?.prefixNode
                    val dataSourceNode = linkFromDataNode?.dataSourceNode
                    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
                    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
                    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
                        scopeContextInExpression = ParadoxScopeHandler.resolveScopeContext(prefixNode, scopeContextInExpression)
                        context.put(PlsCompletionKeys.scopeContextKey, scopeContextInExpression)
                        
                        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
                        val resultToUse = result.withPrefixMatcher(keywordToUse)
                        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
                        val prefix = prefixNode.text
                        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
                    } else {
                        context.put(PlsCompletionKeys.scopeContextKey, scopeContextInExpression)
                        
                        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
                            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
                        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                        val resultToUse = result.withPrefixMatcher(keywordToUse)
                        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
                        if(inFirstNode) {
                            CwtConfigHandler.completeSystemLink(context, resultToUse)
                            CwtConfigHandler.completeScope(context, resultToUse)
                            CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
                        }
                        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
                    }
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeHandler.resolveScopeContext(node, scopeContextInExpression)
                }
            } else if(node is ParadoxDataExpressionNode) {
                if(inRange) {
                    context.put(PlsCompletionKeys.scopeContextKey, scopeContextInExpression)
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.put(PlsCompletionKeys.keywordKey, keywordToUse)
                    CwtConfigHandler.completeSystemLink(context, resultToUse)
                    CwtConfigHandler.completeScope(context, resultToUse)
                    CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
                    CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, node)
                    CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, node, variableOnly = true)
                    break
                }
            }
        }
        
        context.put(PlsCompletionKeys.keywordKey, keyword)
        context.put(PlsCompletionKeys.isKeyKey, isKey)
        context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
    }
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxVariableFieldExpression? {
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
            else -> ParadoxScopeExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
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