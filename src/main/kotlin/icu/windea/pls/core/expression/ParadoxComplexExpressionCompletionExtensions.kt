package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*

/**
 * @return 是否已经输入了前缀。
 */
fun completeForScopeExpressionNode(node: ParadoxScopeExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val offsetInParent = context.offsetInParent
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxScopeLinkFromDataExpressionNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.resolveScopeContext(prefixNode, context.scopeContext)
        context.put(PlsCompletionKeys.scopeContextKey, scopeContextResult)
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
        val prefix = prefixNode.text
        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
        if(inFirstNode) {
            CwtConfigHandler.completeSystemScope(context, resultToUse)
            CwtConfigHandler.completeScope(context, resultToUse)
            CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
        }
        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        return false
    }
}

/**
 * @return 是否已经输入了前缀。
 */
fun completeForValueExpressionNode(node: ParadoxValueFieldExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val offsetInParent = context.offsetInParent
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.resolveScopeContext(prefixNode, context.scopeContext)
        context.put(PlsCompletionKeys.scopeContextKey, scopeContextResult)
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
        val prefix = prefixNode.text
        CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.put(PlsCompletionKeys.keywordKey, keywordToUse)
        if(inFirstNode) {
            CwtConfigHandler.completeValueLinkValue(context, resultToUse)
            CwtConfigHandler.completeValueLinkPrefix(context, resultToUse)
        }
        CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        return false
    }
}

fun completeForVariableDataExpressionNode(node: ParadoxDataExpressionNode, context: ProcessingContext, result: CompletionResultSet) {
    val offsetInParent = context.offsetInParent
    val nodeRange = node.rangeInExpression
    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
    val resultToUse = result.withPrefixMatcher(keywordToUse)
    context.put(PlsCompletionKeys.keywordKey, keywordToUse)
    CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, node, variableOnly = true)
}