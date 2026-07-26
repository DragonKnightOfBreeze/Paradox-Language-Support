package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.prefixFromArgument
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.AddParenthesesInsertHandler
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.toListOrThis
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withState
import icu.windea.pls.lang.defineNamespaceInfo
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeContext

object ParadoxComplexExpressionCompletionManager {
    // region Entry Completion Methods

    fun completeTemplateExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val finalConfig = context.configs.firstOrNull() ?: context.config
        if (finalConfig == null) return
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxTemplateExpression.resolve(context.keyword, textRange, context.configGroup, finalConfig) } ?: return
        completeForTemplateExpression(context, result, expression)
    }

    fun completeScopeFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxScopeFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForScopeFieldExpression(context, result, expression)
    }

    fun completeValueFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxValueFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForValueFieldExpression(context, result, expression)
    }

    fun completeVariableFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxVariableFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForVariableFieldExpression(context, result, expression)
    }

    fun completeCommandExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxCommandExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForCommandExpression(context, result, expression)
    }

    fun completeDynamicValueExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val finalConfigs = if (context.configs.isNotEmpty()) context.configs.toListOrThis() else context.config.to.singletonListOrEmpty()
        if (finalConfigs.isEmpty()) return
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDynamicValueExpression.resolve(context.keyword, textRange, context.configGroup, finalConfigs) } ?: return
        completeForDynamicValueExpression(context, result, expression)
    }

    fun completeScriptValueReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxScriptValueReferenceExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForScriptValueReferenceExpression(context, result, expression)
    }

    fun completeDefineReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDefineReferenceExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForDefineReferenceExpression(context, result, expression)
    }

    fun completeArrayDefineReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxArrayDefineReferenceExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForArrayDefineReferenceExpression(context, result, expression)
    }

    fun completeTagsExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // 2.1.10 compatible with `not(...)`
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val config = context.config ?: return
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxTagsExpression.resolve(context.keyword, textRange, context.configGroup, config) } ?: return
        completeForTagsExpression(context, result, expression)
    }

    fun completeDatabaseObjectExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDatabaseObjectExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return
        completeForDatabaseObjectExpression(context, result, expression)
    }

    fun completeNameFormatExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        if (context.offsetInExpression < 0) return // unexpected
        val config = context.config ?: return
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxNameFormatExpression.resolve(context.keyword, textRange, context.configGroup, config) } ?: return
        completeForNameFormatExpression(context, result, expression)
    }

    private inline fun <T> markIncomplete(action: () -> T): T {
        return withState(ChronicleThreadContext.incompleteComplexExpression, action)
    }

    // endregion

    // region Navigation Completion Methods

    private fun completeForTemplateExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxTemplateExpression) {
        val context = context.copy(isKey = null, scopeContext = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxTemplateSnippetNode -> completeForTemplateSnippetNode(context, result, node)
                is ParadoxTemplateSnippetConstantNode -> completeForTemplateSnippetConstantNode(context, result, node)
            }
        }
    }

    private fun completeForTemplateSnippetNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxTemplateSnippetNode) {
        val config = node.getMockConfig()
        val context = context.copyFromNode(node).copy(config = config, configs = emptyList())
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
    }

    private fun completeForTemplateSnippetConstantNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxTemplateSnippetConstantNode) {
        // 一般来说，仅适用于是第一个节点的情况（否则，仍然会匹配范围内的通配符）
        val config = node.getMockConfig()
        val context = context.copyFromNode(node).copy(config = config, configs = emptyList())
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxExpressionCompletionManager.completeConstant(context, result)
    }

    private fun completeForScopeFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxScopeFieldExpression) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextFromNode = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
                if (node is ParadoxScopeNode) scopeContextFromNode = scopeContextFromNode.switchFromNode(node, element)  // switch scope context
                continue
            }
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxScopeNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    completeForScopeNode(context, result, node)
                }
            }
            break // about process
        }
    }

    private fun completeForValueFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxValueFieldExpression) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextFromNode = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
                if (node is ParadoxScopeNode) scopeContextFromNode = scopeContextFromNode.switchFromNode(node, element)  // switch scope context
                continue
            }
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxScopeNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    completeForScopeNode(context, result, node)
                }
                is ParadoxValueFieldNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    val scopeNode = ParadoxScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForValueFieldNode(context, result, node)
                }
            }
            break // about process
        }
    }

    private fun completeForVariableFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxVariableFieldExpression) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextFromNode = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
                if (node is ParadoxScopeNode) scopeContextFromNode = scopeContextFromNode.switchFromNode(node, element)  // switch scope context
                continue
            }
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxScopeNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    completeForScopeNode(context, result, node)
                }
                is ParadoxDataSourceNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    val scopeNode = ParadoxScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForVariableFieldValueNode(context, result, node)
                }
            }
            break // about process
        }
    }

    /** @return 是否已经输入了前缀。 */
    private fun completeForScopeNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScopeNode): Boolean {
        var inputPrefix = false
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        val dynamicScopeNode = node.castOrNull<ParadoxDynamicScopeNode>()
        val prefixNode = dynamicScopeNode?.prefixNode
        val valueNode = dynamicScopeNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argumentIndex = valueNode?.getArgumentIndex(context.offsetInExpression) ?: 0
        val currentArgumentNode = valueNode?.argumentNodes?.getOrNull(argumentIndex)
        if (prefixNode != null && valueNode != null && context.offsetInExpression >= valueNode.rangeInExpression.startOffset) {
            val keywordNode = currentArgumentNode ?: valueNode
            val scopeContext = scopeContext.switchFromNode(node, element)
            val context = context.copyFromNode(keywordNode).copy(linkArgumentIndex = argumentIndex, scopeContext = scopeContext)
            val result = result.withPrefixMatcher(context.keyword)
            completeScopeValue(context, result, prefixNode.text, currentArgumentNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || context.offsetInExpression <= valueNode.nodes.first().rangeInExpression.endOffset
            val context = context.copyFromNode(node).copy(linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(context.keyword)
            if (inFirstNode) {
                completeSystemScope(context, result)
                completeStaticScope(context, result)
                completeScopePrefix(context, result)
            }
            completeScopeValue(context, result, null, currentArgumentNode)
        }
        return inputPrefix
    }

    /** @return 是否已经输入了前缀。 */
    private fun completeForValueFieldNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxValueFieldNode): Boolean {
        var inputPrefix = false
        val fieldNode = node.castOrNull<ParadoxDynamicValueFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argumentIndex = valueNode?.getArgumentIndex(context.offsetInExpression) ?: 0
        val currentArgumentNode = valueNode?.argumentNodes?.getOrNull(argumentIndex)
        if (prefixNode != null && valueNode != null && context.offsetInExpression >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文
            val keywordNode = currentArgumentNode ?: valueNode
            val context = context.copyFromNode(keywordNode).copy(linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(context.keyword)
            completeValueFieldValue(context, result, prefixNode.text, currentArgumentNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || context.offsetInExpression <= valueNode.nodes.first().rangeInExpression.endOffset
            val context = context.copyFromNode(node).copy(linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(context.keyword)
            if (inFirstNode) {
                completeStaticValueField(context, result)
                completeValueFieldPrefix(context, result)
            }
            completeValueFieldValue(context, result, null, currentArgumentNode)
        }
        return inputPrefix
    }

    private fun completeForVariableFieldValueNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxDataSourceNode) {
        val context = context.copyFromNode(node)
        val result = result.withPrefixMatcher(context.keyword)
        completeValueFieldValue(context, result, null, node, variableOnly = true)
    }

    private fun completeForCommandExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxCommandExpression) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextFromNode = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
                if (node is ParadoxCommandScopeNode) scopeContextFromNode = scopeContextFromNode.switchFromNode(node, element)  // switch scope context
                continue
            }
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxCommandScopeNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    completeForCommandScopeNode(context, result, node)
                }
                is ParadoxCommandFieldNode -> {
                    val context = context.copy(scopeContext = scopeContextFromNode)
                    val scopeNode = ParadoxCommandScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForCommandScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForCommandFieldNode(context, result, node)
                }
            }
            break // about process
        }
    }

    /** @return 是否已经输入了前缀。 */
    private fun completeForCommandScopeNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxCommandScopeNode): Boolean {
        var inputPrefix = false
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        val dynamicScopeNode = node.castOrNull<ParadoxDynamicCommandScopeNode>()
        val prefixNode = dynamicScopeNode?.prefixNode
        val valueNode = dynamicScopeNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argumentIndex = valueNode?.getArgumentIndex(context.offsetInExpression) ?: 0
        val currentArgumentNode = valueNode?.argumentNodes?.getOrNull(argumentIndex)
        if (prefixNode != null && valueNode != null && context.offsetInExpression >= valueNode.rangeInExpression.startOffset) {
            val keywordNode = currentArgumentNode ?: valueNode
            val scopeContext = scopeContext.switchFromNode(node, element)
            val context = context.copyFromNode(keywordNode).copy(linkArgumentIndex = argumentIndex, scopeContext = scopeContext)
            val result = result.withPrefixMatcher(context.keyword)
            completeCommandScopeValue(context, result, prefixNode.text, currentArgumentNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || context.offsetInExpression <= valueNode.nodes.first().rangeInExpression.endOffset
            val context = context.copyFromNode(node).copy(linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(context.keyword)
            if (inFirstNode) {
                completeSystemCommandScope(context, result)
                completeStaticCommandScope(context, result)
                completeCommandScopePrefix(context, result)
            }
            completeCommandScopeValue(context, result, null, currentArgumentNode)
        }
        return inputPrefix
    }

    /** @return 是否已经输入了前缀。 */
    private fun completeForCommandFieldNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxCommandFieldNode): Boolean {
        var inputPrefix = false
        val fieldNode = node.castOrNull<ParadoxDynamicCommandFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argumentIndex = valueNode?.getArgumentIndex(context.offsetInExpression) ?: 0
        val currentArgumentNode = valueNode?.argumentNodes?.getOrNull(argumentIndex)
        if (prefixNode != null && valueNode != null && context.offsetInExpression >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文
            val keywordNode = currentArgumentNode ?: valueNode
            val keywordOffset = keywordNode.rangeInExpression.startOffset
            val keyword = keywordNode.text.substring(0, context.offsetInExpression - keywordOffset)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(keyword)
            completeCommandFieldValue(context, result, prefixNode.text, currentArgumentNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || context.offsetInExpression <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordOffset = node.rangeInExpression.startOffset
            val keyword = node.text.substring(0, context.offsetInExpression - keywordOffset)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgumentIndex = argumentIndex)
            val result = result.withPrefixMatcher(keyword)
            if (inFirstNode) {
                completeStaticCommandField(context, result)
                completeCommandFieldPrefix(context, result)
            }
            completeCommandFieldValue(context, result, null, currentArgumentNode)
        }
        return inputPrefix
    }

    private fun completeForDynamicValueExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxDynamicValueExpression) {
        // skip check scope context here
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxDynamicValueNode -> completeForDynamicValueNode(context, result, node)
                is ParadoxScopeFieldExpression -> completeForDynamicValueNestedScopeFieldExpression(context, result, node)
            }
            break // about process
        }
    }

    private fun completeForDynamicValueNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxDynamicValueNode) {
        val context = context.copyFromNode(node).copy(config = node.configs.firstOrNull(), configs = node.configs)
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
    }

    private fun completeForDynamicValueNestedScopeFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScopeFieldExpression) {
        val context = context.copyFromNode(node)
        val result = result.withPrefixMatcher(context.keyword)
        completeScopeFieldExpression(context, result)
    }

    private fun completeForScriptValueReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxScriptValueReferenceExpression) {
        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val context = context.copy(isKey = null, scopeContext = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxScriptValueNode -> completeForScriptValueNode(context, result, node)
                is ParadoxScriptValueArgumentNameNode -> completeForScriptValueArgumentNode(context, result, node, element)
                is ParadoxScriptValueArgumentValueNode -> completeForScopeValueArgumentValueNode(context, result, node, element)
            }
            break // about process
        }
    }

    private fun completeForScriptValueNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScriptValueNode) {
        val config = node.config
        val context = context.copyFromNode(node).copy(config = config, configs = emptyList())
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
    }

    private fun completeForScriptValueArgumentNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScriptValueArgumentNameNode, element: ParadoxExpressionElement) {
        val expression = node.parent as? ParadoxScriptValueReferenceExpression ?: return // unexpected
        if (expression.scriptValueNode.text.isEmpty()) return
        // 提示参数的名字
        val context = context.copyFromNode(node)
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxParameterManager.completeArguments(context, result, element)
    }

    private fun completeForScopeValueArgumentValueNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScriptValueArgumentValueNode, element: ParadoxExpressionElement) {
        if (!ChronicleSettings.getInstance().state.inference.configContextForParameters) return
        val expression = node.parent as? ParadoxScriptValueReferenceExpression ?: return // unexpected
        if (expression.scriptValueNode.text.isEmpty()) return
        // 尝试提示传入参数的值
        val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return
        val inferredContextConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
        val inferredConfig = inferredContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>() ?: return
        val context = context.copyFromNode(node).copy(config = inferredConfig, configs = emptyList())
        val result = result.withPrefixMatcher(context.keyword)
        ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
    }

    private fun completeForDefineReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxDefineReferenceExpression) {
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxDefineNamespaceNode -> completeForDefineNamespaceNode(context, result, node)
                is ParadoxDefineVariableNode -> completeForDefineVariableNode(context, result, node)
            }
        }
    }

    private fun completeForArrayDefineReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxArrayDefineReferenceExpression) {
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxDefineNamespaceNode -> completeForDefineNamespaceNode(context, result, node)
                is ParadoxDefineVariableNode -> completeForDefineVariableNode(context, result, node)
            }
            break // about process
        }
    }

    private fun completeForDefineNamespaceNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxDefineNamespaceNode) {
        val context = context.copyFromNode(node)
        val result = result.withPrefixMatcher(context.keyword)
        completeDefineNamespace(context, result)
    }

    private fun completeForDefineVariableNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxDefineVariableNode) {
        val context = context.copyFromNode(node)
        val result = result.withPrefixMatcher(context.keyword)
        completeDefineVariable(context, result)
    }

    private fun completeForTagsExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxTagsExpression) {
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
            if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
            ProgressManager.checkCanceled()
            when (node) {
                is ParadoxDynamicValueNode -> {
                    val context = context.copyFromNode(node).copy(config = node.configs.firstOrNull(), configs = node.configs)
                    val result = result.withPrefixMatcher(context.keyword)
                    completeNegated(context, result)
                    ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
                }
                is ParadoxNegatedDynamicValueNode -> {
                    val condition = expression.config.configExpression?.metadata?.condition ?: false
                    if (!condition) continue // skip if is not a condition variant
                    for (node in node.nodes) {
                        if (context.offsetInExpression > node.rangeInExpression.endOffset) break // about process
                        if (context.offsetInExpression < node.rangeInExpression.startOffset) continue // continue process root nodes
                        ProgressManager.checkCanceled()
                        when (node) {
                            is ParadoxDynamicValueNode -> {
                                val context = context.copyFromNode(node).copy(config = node.configs.firstOrNull(), configs = node.configs)
                                val result = result.withPrefixMatcher(context.keyword)
                                ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
                            }
                        }
                        break // about process
                    }
                }
            }
            break // about process
        }
    }

    private fun completeForDatabaseObjectExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxDatabaseObjectExpression) {
        val context = context.copy(isKey = null)
        expression.acceptChildren(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (context.offsetInExpression > node.rangeInExpression.endOffset) return false // about process
                if (context.offsetInExpression < node.rangeInExpression.startOffset) return true // continue process root nodes
                ProgressManager.checkCanceled()
                when (node) {
                    is ParadoxDatabaseObjectTypeNode -> {
                        val context = context.copyFromNode(node)
                        val result = result.withPrefixMatcher(context.keyword)
                        completeDatabaseObjectType(context, result)
                        return false
                    }
                    is ParadoxDatabaseObjectValueNode -> {
                        val context = context.copyFromNode(node)
                        val result = result.withPrefixMatcher(context.keyword)
                        completeDatabaseObject(context, result)
                        return false
                    }
                }
                return super.visit(node) // continue process nodes recursively
            }
        })
    }

    private fun completeForNameFormatExpression(context: ParadoxCompletionContext, result: CompletionResultSet, expression: ParadoxNameFormatExpression) {
        val context = context.copy(isKey = null)
        expression.acceptChildren(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (context.offsetInExpression > node.rangeInExpression.endOffset) return false // about process
                if (context.offsetInExpression < node.rangeInExpression.startOffset) return true // continue process root nodes
                ProgressManager.checkCanceled()
                when (node) {
                    is ParadoxNameFormatDefinitionNode -> {
                        val mockConfig = node.getMockConfig() ?: return false
                        val context = context.copyFromNode(node).copy(config = mockConfig)
                        val result = result.withPrefixMatcher(context.keyword)
                        ParadoxExpressionCompletionManager.completeDefinition(context, result)
                        return false
                    }
                    is ParadoxNameFormatLocalisationNode -> {
                        val mockConfig = node.getMockConfig()
                        val context = context.copyFromNode(node).copy(config = mockConfig)
                        val result = result.withPrefixMatcher(context.keyword)
                        ParadoxExpressionCompletionManager.completeLocalisation(context, result)
                        return false
                    }
                    is ParadoxCommandExpression -> {
                        val context = context.copyFromNode(node)
                        val result = result.withPrefixMatcher(context.keyword)
                        completeCommandExpression(context, result)
                        return false
                    }
                }
                return super.visit(node) // continue process nodes recursively
            }
        })
    }

    private fun ParadoxCompletionContext.copyFromNode(node: ParadoxComplexExpressionNode): ParadoxCompletionContext {
        val keyword = node.text.substring(0, offsetInExpression - node.rangeInExpression.startOffset)
        val keywordOffset = node.rangeInExpression.startOffset
        return copy(keyword = keyword, keywordOffset = keywordOffset, node = node)
    }

    private fun ParadoxScopeContext.switchFromNode(node: ParadoxComplexExpressionNode, element: ParadoxExpressionElement): ParadoxScopeContext {
        return ParadoxScopeManager.getScopeContext(element, node, this)
    }

    // endregion

    // region General Completion Methods

    private fun completeNegated(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val name = "not"
        val tailText = "(...)"
        val lookupElement = LookupElementBuilder.create(name)
            .withBoldness(true)
            .withTailText(tailText, true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withPriority(ParadoxCompletionPriorities.keyword)
            .withCompletionId()
        result.addElement(lookupElement, context)
    }

    private fun completeSystemScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        // 总是提示，无论作用域是否匹配
        val systemScopeConfigs = context.configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            ProgressManager.checkCanceled()
            val name = systemScopeConfig.name
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withPriority(ParadoxCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeStaticScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linksConfigs = context.configGroup.linksModel.forScopeStatic
        for (linkConfig in linksConfigs) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.StaticScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeScopePrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.linksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(AddParenthesesInsertHandler())
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.linksModel.forScopeFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeScopeValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argumentNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argumentNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.links.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgumentIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argumentNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    private fun completeStaticValueField(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigs = context.configGroup.linksModel.forValueStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            // 排除 input_scopes 不匹配前一个 scope 的 output_scope 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.StaticValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeValueFieldPrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.linksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(AddParenthesesInsertHandler())
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.linksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeValueFieldValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argumentNode: ParadoxComplexExpressionNode?, variableOnly: Boolean = false) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argumentNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = if (variableOnly) context.configGroup.linksModel.variable
        else context.configGroup.links.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgumentIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argumentNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            is ParadoxScriptValueReferenceExpression -> completeScriptValueReferenceExpression(context, result)
            is ParadoxDefineReferenceExpression -> completeDefineReferenceExpression(context, result)
            is ParadoxArrayDefineReferenceExpression -> completeArrayDefineReferenceExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    private fun completeSystemCommandScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        // 总是提示，无论作用域是否匹配
        val systemScopeConfigs = context.configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            ProgressManager.checkCanceled()
            val name = systemScopeConfig.name
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.SystemCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withPriority(ParadoxCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeStaticCommandScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigs = context.configGroup.localisationLinksModel.forScopeStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            // optimize: make first char uppercase (e.g., owner -> Owner)
            val name = linkConfig.name.replaceFirstChar { it.uppercaseChar() }
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.StaticCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeCommandScopePrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.localisationLinksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(AddParenthesesInsertHandler())
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.localisationLinksModel.forScopeFromDataSorted
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeCommandScopeValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argumentNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argumentNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.localisationLinks.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgumentIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argumentNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    private fun completeStaticCommandField(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val localisationCommands = context.configGroup.localisationCommands
        for (localisationCommand in localisationCommands.values) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, localisationCommand.supportedScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.StaticCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigs = context.configGroup.localisationLinksModel.forValueStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            // 排除 input_scopes 不匹配前一个 scope 的 output_scope 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.StaticCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeCommandFieldPrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.localisationLinksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(AddParenthesesInsertHandler())
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.localisationLinksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(ChronicleIcons.Nodes.DynamicCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    private fun completeCommandFieldValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argumentNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argumentNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.localisationLinks.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgumentIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argumentNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    private fun completeScriptExpressionFromLinkConfigs(context: ParadoxCompletionContext, result: CompletionResultSet, linkConfigs: List<CwtLinkConfig>) {
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            val context = context.copy(config = linkConfig)
            ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
        }
    }

    private fun completeDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val project = context.parameters.originalFile.project
        val contextElement = context.contextElement
        val tailText = " from define namespaces"
        val selector = ParadoxDefineNamespaceSearch.selector(project, contextElement).distinct()
        ParadoxDefineNamespaceSearch.search(null, selector).processAsync p@{ element ->
            ProgressManager.checkCanceled()
            val defineInfo = element.defineNamespaceInfo ?: return@p true
            val namespace = defineInfo.namespace
            val lookupElement = LookupElementBuilder.create(element, namespace)
                .withPatchableIcon(ChronicleIcons.Nodes.DefineNamespace)
                .withPatchableTailText(tailText)
                .wrapForExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    private fun completeDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val project = context.parameters.originalFile.project
        val contextElement = context.contextElement
        val node = context.node?.castOrNull<ParadoxDefineVariableNode>() ?: return
        val namespaceNode = node.namespaceNode ?: return
        val namespace = namespaceNode.text
        val tailText = " from define namespace ${namespace}"
        val selector = ParadoxDefineVariableSearch.selector(project, contextElement).distinct()
        ParadoxDefineVariableSearch.search(namespace, null, selector).processAsync p@{ element ->
            ProgressManager.checkCanceled()
            val defineInfo = element.defineVariableInfo ?: return@p true
            val variable = defineInfo.variable
            val lookupElement = LookupElementBuilder.create(element, variable)
                .withPatchableIcon(ChronicleIcons.Nodes.DefineVariable)
                .withPatchableTailText(tailText)
                .wrapForExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    private fun completeDatabaseObjectType(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val tailText = " from database object types"
        val configs = context.configGroup.databaseObjectTypes.values
        for (config in configs) {
            ProgressManager.checkCanceled()
            val name = config.name
            val element = config.pointer.element ?: continue
            val typeFile = config.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(ChronicleIcons.Nodes.DatabaseObjectType)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ParadoxCompletionPriorities.prefix)
                .withPatchableTailText(tailText)
                .wrapForExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    private fun completeDatabaseObject(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val node = context.node?.castOrNull<ParadoxDatabaseObjectValueNode>()?.nodes?.findIsInstance<ParadoxDatabaseObjectNode>() ?: return
        val config = node.config ?: return

        val typeToSearch = node.getTypeToSearch()
        if (typeToSearch == null) return

        val expressionTailText = " from database object type ${config.name}"
        val context = context.copy(patchableTailText = expressionTailText)

        // complete forced base database object
        completeForcedBaseDatabaseObject(context, result, node)

        run {
            val mockConfig = config.getConfigForType(node.isBase)
            val extraFilter = { e: PsiElement -> node.isValidDatabaseObject(e, typeToSearch) }
            val context = context.copy(config = mockConfig, extraFilter = extraFilter)
            if (config.localisation != null) {
                ParadoxExpressionCompletionManager.completeLocalisation(context, result)
            } else {
                ParadoxExpressionCompletionManager.completeDefinition(context, result)
            }
        }
    }

    private fun completeForcedBaseDatabaseObject(context: ParadoxCompletionContext, result: CompletionResultSet, dsNode: ParadoxDatabaseObjectNode) {
        val config = dsNode.config ?: return
        if (!dsNode.isPossibleForcedBase()) return
        val valueNode = dsNode.expression.valueNode ?: return
        val selector = ParadoxDefinitionSearch.selector(context.project, context.contextElement).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchElement(valueNode.text, config.type, selector).processAsync {
            ParadoxCompletionLookupProvider.processDefinition(context, result, it)
        }
    }

    // endregion
}
