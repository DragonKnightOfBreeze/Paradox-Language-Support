package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*

/**
 * @return 是否已经输入了前缀。
 */
fun completeForScopeExpressionNode(node: ParadoxScopeFieldExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxScopeLinkFromDataExpressionNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    val offsetInParent = context.offsetInParent
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.resolveScopeContext(prefixNode, context.scopeContext)
        context.scopeContext = scopeContextResult
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        val keyword = context.keyword
        val startOffset = context.startOffset
        context.keyword = keywordToUse
        context.startOffset = dataSourceNode.rangeInExpression.startOffset
        val prefix = prefixNode.text
        ParadoxConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        context.keyword = keyword
        context.startOffset = startOffset
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        val keyword = context.keyword
        val startOffset = context.startOffset
        context.keyword = keywordToUse
        context.startOffset = node.rangeInExpression.startOffset
        if(inFirstNode) {
            ParadoxConfigHandler.completeSystemScope(context, resultToUse)
            ParadoxConfigHandler.completeScope(context, resultToUse)
            ParadoxConfigHandler.completeScopeLinkPrefix(context, resultToUse)
        }
        ParadoxConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.startOffset = startOffset
        return false
    }
}

/**
 * @return 是否已经输入了前缀。
 */
fun completeForValueExpressionNode(node: ParadoxValueFieldExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val keyword = context.keyword
    val startOffset = context.startOffset
    val offsetInParent = context.offsetInParent
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.resolveScopeContext(prefixNode, context.scopeContext)
        context.scopeContext = scopeContextResult
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.startOffset = dataSourceNode.rangeInExpression.startOffset
        val prefix = prefixNode.text
        ParadoxConfigHandler.completeValueLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        context.keyword = keyword
        context.startOffset = startOffset
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.startOffset = node.rangeInExpression.startOffset
        if(inFirstNode) {
            ParadoxConfigHandler.completeValueLinkValue(context, resultToUse)
            ParadoxConfigHandler.completeValueLinkPrefix(context, resultToUse)
        }
        ParadoxConfigHandler.completeValueLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.startOffset = startOffset
        return false
    }
}

fun completeForVariableDataExpressionNode(node: ParadoxDataExpressionNode, context: ProcessingContext, result: CompletionResultSet) {
    val offsetInParent = context.offsetInParent
    val nodeRange = node.rangeInExpression
    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
    val resultToUse = result.withPrefixMatcher(keywordToUse)
    context.keyword = keywordToUse
    ParadoxConfigHandler.completeValueLinkDataSource(context, resultToUse, null, node, variableOnly = true)
}