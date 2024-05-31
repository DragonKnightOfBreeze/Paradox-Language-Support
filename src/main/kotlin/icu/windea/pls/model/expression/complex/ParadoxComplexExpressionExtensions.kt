package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

fun ParadoxComplexExpression.processAllNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    return doProcessAllNodes(processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    val r = processor.process(this)
    if(!r) return false
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            val r1 = node.doProcessAllNodes(processor)
            if(!r1) return false
        }
    }
    return true
}

fun ParadoxComplexExpression.processAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    return doProcessAllLeafNodes(processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            val r1 = node.doProcessAllLeafNodes(processor)
            if(!r1) return false
        }
        return true
    } else {
        return processor.process(this)
    }
}

fun ParadoxComplexExpression.getReferences(element: ParadoxScriptStringExpressionElement): Array<PsiReference> {
    val references = mutableListOf<PsiReference>()
    this.doGetReferences(element, references)
    return references.toTypedArray()
}

private fun ParadoxComplexExpressionNode.doGetReferences(element: ParadoxScriptStringExpressionElement, references: MutableList<PsiReference>) {
    val reference = this.getReference(element)
    if(reference != null) {
        references.add(reference)
    }
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            node.doGetReferences(element, references)
        }
    }
}


/**
 * @return 是否已经输入了前缀。
 */
fun completeForScopeNode(node: ParadoxScopeFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val contextElement = context.contextElement!!
    val offsetInParent = context.offsetInParent!!
    val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxScopeLinkFromDataNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.getScopeContext(contextElement, prefixNode, scopeContext)
        context.scopeContext = scopeContextResult
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        context.keyword = keywordToUse
        context.keywordOffset = dataSourceNode.rangeInExpression.startOffset
        val prefix = prefixNode.text
        ParadoxCompletionManager.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        context.keyword = keywordToUse
        context.keywordOffset = node.rangeInExpression.startOffset
        if(inFirstNode) {
            ParadoxCompletionManager.completeSystemScope(context, resultToUse)
            ParadoxCompletionManager.completeScope(context, resultToUse)
            ParadoxCompletionManager.completeScopeLinkPrefix(context, resultToUse)
        }
        ParadoxCompletionManager.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return false
    }
}

/**
 * @return 是否已经输入了前缀。
 */
fun completeForValueNode(node: ParadoxValueFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val contextElement = context.contextElement!!
    val keyword = context.keyword
    val keywordOffset = context.keywordOffset
    val offsetInParent = context.offsetInParent!!
    val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataNode>()
    val prefixNode = linkFromDataNode?.prefixNode
    val dataSourceNode = linkFromDataNode?.dataSourceNode
    val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
    val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
    if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
        val scopeContextResult = ParadoxScopeHandler.getScopeContext(contextElement, prefixNode, scopeContext)
        context.scopeContext = scopeContextResult
        
        val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.keywordOffset = dataSourceNode.rangeInExpression.startOffset
        val prefix = prefixNode.text
        ParadoxCompletionManager.completeValueLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return true
    } else {
        val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
            || offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.keywordOffset = node.rangeInExpression.startOffset
        if(inFirstNode) {
            ParadoxCompletionManager.completeValueLinkValue(context, resultToUse)
            ParadoxCompletionManager.completeValueLinkPrefix(context, resultToUse)
        }
        ParadoxCompletionManager.completeValueLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return false
    }
}

fun completeForVariableDataSourceNode(node: ParadoxDataSourceNode, context: ProcessingContext, result: CompletionResultSet) {
    val offsetInParent = context.offsetInParent!!
    val nodeRange = node.rangeInExpression
    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
    val resultToUse = result.withPrefixMatcher(keywordToUse)
    context.keyword = keywordToUse
    ParadoxCompletionManager.completeValueLinkDataSource(context, resultToUse, null, node, variableOnly = true)
}