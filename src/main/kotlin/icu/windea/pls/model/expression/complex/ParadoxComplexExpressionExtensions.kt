package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

fun ParadoxComplexExpression.processAllNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
    return doProcessAllNodes(processor)
}

private fun ParadoxExpressionNode.doProcessAllNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
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

fun ParadoxComplexExpression.processAllLeafNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
    return doProcessAllLeafNodes(processor)
}

private fun ParadoxExpressionNode.doProcessAllLeafNodes(processor: Processor<ParadoxExpressionNode>): Boolean {
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

private fun ParadoxExpressionNode.doGetReferences(element: ParadoxScriptStringExpressionElement, references: MutableList<PsiReference>) {
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
fun completeForScopeExpressionNode(node: ParadoxScopeFieldExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val contextElement = context.contextElement!!
    val offsetInParent = context.offsetInParent!!
    val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxScopeLinkFromDataExpressionNode>()
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
        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
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
            CwtConfigHandler.completeSystemScope(context, resultToUse)
            CwtConfigHandler.completeScope(context, resultToUse)
            CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
        }
        CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return false
    }
}

/**
 * @return 是否已经输入了前缀。
 */
fun completeForValueExpressionNode(node: ParadoxValueFieldExpressionNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
    val contextElement = context.contextElement!!
    val keyword = context.keyword
    val keywordOffset = context.keywordOffset
    val offsetInParent = context.offsetInParent!!
    val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
    val nodeRange = node.rangeInExpression
    val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
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
        CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
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
            CwtConfigHandler.completeValueLinkValue(context, resultToUse)
            CwtConfigHandler.completeValueLinkPrefix(context, resultToUse)
        }
        CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        return false
    }
}

fun completeForVariableDataExpressionNode(node: ParadoxDataExpressionNode, context: ProcessingContext, result: CompletionResultSet) {
    val offsetInParent = context.offsetInParent!!
    val nodeRange = node.rangeInExpression
    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
    val resultToUse = result.withPrefixMatcher(keywordToUse)
    context.keyword = keywordToUse
    CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, node, variableOnly = true)
}