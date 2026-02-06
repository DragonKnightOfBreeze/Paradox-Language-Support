package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.prefixFromArgument
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.toListOrThis
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withState
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.namespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.scriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.valueNode
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctByExpression
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexExpressionCompletionManager {
    // region Core Methods

    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config
        val configs = context.configs

        val finalConfig = configs.firstOrNull() ?: config
        if (finalConfig == null) return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxTemplateExpression.resolve(keyword, textRange, configGroup, finalConfig) } ?: return

        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.scopeContext = null // skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxTemplateSnippetNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = node.config
                    context.configs = emptyList()
                    ParadoxCompletionManager.completeScriptExpression(context, resultToUse)
                    break
                }
            } else if (node is ParadoxTemplateSnippetConstantNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    // 一般来说，仅适用于是第一个节点的情况（否则，仍然会匹配范围内的通配符）
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = CwtValueConfig.createMock(configGroup, node.text)
                    context.configs = emptyList()
                    ParadoxCompletionManager.completeConstant(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeDynamicValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config
        val configs = context.configs

        val finalConfigs = if (configs.isNotEmpty()) configs.toListOrThis() else config.to.singletonListOrEmpty()
        if (finalConfigs.isEmpty()) return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDynamicValueExpression.resolve(keyword, textRange, configGroup, finalConfigs) } ?: return

        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.config = expression.configs.first()
        context.configs = expression.configs
        context.scopeContext = null // skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDynamicValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    ParadoxCompletionManager.completeDynamicValue(context, resultToUse)
                    break
                }
            } else if (node is ParadoxScopeFieldExpression) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    completeScopeFieldExpression(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeScriptValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return
        val config = context.config ?: return
        val configs = context.configs

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxScriptValueExpression.resolve(keyword, textRange, configGroup, config) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.scopeContext = null // skip check scope context here
        context.isKey = null
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxScriptValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.config = expression.config
                    context.configs = emptyList()
                    ParadoxCompletionManager.completeScriptExpression(context, resultToUse)
                    context.config = config
                    context.configs = configs
                }
            } else if (node is ParadoxScriptValueArgumentNode) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    ParadoxParameterManager.completeArguments(element, context, resultToUse)
                }
            } else if (node is ParadoxScriptValueArgumentValueNode && PlsSettings.getInstance().state.inference.configContextForParameters) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    // 尝试提示传入参数的值
                    run {
                        val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                        val resultToUse = result.withPrefixMatcher(keywordToUse)
                        val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return@run
                        val inferredContextConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
                        val inferredConfig = inferredContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>() ?: return@run
                        context.keyword = keywordToUse
                        context.keywordOffset = node.rangeInExpression.startOffset
                        context.config = inferredConfig
                        context.configs = emptyList()
                        ParadoxCompletionManager.completeScriptExpression(context, resultToUse)
                        context.config = config
                        context.configs = configs
                    }
                }
            }
        }
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxValueFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForValueFieldNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxDataSourceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForVariableFieldValueNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeCommandExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxCommandExpression.resolve(keyword, textRange, configGroup) } ?: return

        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val isKey = context.isKey
        context.isKey = null
        var scopeContextInExpression = scopeContext
        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxCommandScopeLinkNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    completeForCommandScopeLinkNode(node, context, result)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxCommandFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxCommandScopeLinkNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForCommandScopeLinkNode(scopeNode, context, result)
                    if (afterPrefix) break
                    completeForCommandFieldNode(node, context, result)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }

    fun completeDatabaseObjectExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDatabaseObjectExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null

        for (node in expression.nodes) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDatabaseObjectTypeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDatabaseObjectType(context, resultToUse)
                    break
                }
            } else if (node is ParadoxDatabaseObjectNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDatabaseObject(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.node = oldNode
        context.isKey = isKey
    }

    fun completeDefineReferenceExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val configGroup = context.configGroup ?: return

        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        val expression = markIncomplete { ParadoxDefineReferenceExpression.resolve(keyword, textRange, configGroup) } ?: return

        val oldNode = context.node
        val isKey = context.isKey
        context.isKey = null
        for (node in expression.nodes) {
            if (node is ParadoxErrorNode && expression.nodes.size == 1) {
                ProgressManager.checkCanceled()
                completeDefinePrefix(context, result)
                break
            }
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDefineNamespaceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDefineNamespace(context, resultToUse)
                    break
                }
            } else if (node is ParadoxDefineVariableNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    context.node = node
                    completeDefineVariable(context, resultToUse)
                    break
                }
            }
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.node = oldNode
        context.isKey = isKey
    }

    fun completeStellarisNameFormatExpression(context: ProcessingContext, result: CompletionResultSet) {
        // TODO 2.0.6+ 并没有基于节点进行代码补全……不过能用就行，暂时不做重构

        val element = context.contextElement as? ParadoxScriptStringExpressionElement ?: return
        val config = context.config ?: return
        val formatName = config.configExpression?.value ?: return
        val type = "${formatName}_name_parts_list"

        // caret position inside expression
        val caretInExpr = context.offsetInParent - context.expressionOffset
        if (caretInExpr < 0) return
        val exprText = element.value
        val caret = caretInExpr.coerceIn(0, exprText.length)

        fun lastUnclosedIndex(open: Char, close: Char, until: Int): Int {
            var depth = 0
            var lastOpen = -1
            var i = 0
            while (i < until) {
                when (exprText[i]) {
                    open -> {
                        depth++; lastOpen = i
                    }
                    close -> if (depth > 0) depth--
                }
                i++
            }
            return if (depth > 0) lastOpen else -1
        }

        // inside [...]
        run {
            val leftSq = lastUnclosedIndex('[', ']', caret)
            if (leftSq >= 0) {
                val innerStart = leftSq + 1
                val keywordToUse = exprText.substring(innerStart, caret)
                val bakKeyword = context.keyword
                val bakKeywordOffset = context.keywordOffset
                val isKey = context.isKey
                context.keyword = keywordToUse
                context.keywordOffset = innerStart
                context.isKey = null
                val resultToUse = result.withPrefixMatcher(keywordToUse)
                ParadoxComplexExpressionCompletionManager.completeCommandExpression(context, resultToUse)
                context.keyword = bakKeyword
                context.keywordOffset = bakKeywordOffset
                context.isKey = isKey
                return
            }
        }

        // inside <...>
        run {
            val leftAngle = lastUnclosedIndex('<', '>', caret)
            if (leftAngle >= 0) {
                val innerStart = leftAngle + 1
                val keywordToUse = exprText.substring(innerStart, caret)
                val cfg = CwtValueConfig.createMock(config.configGroup, "<${type}>")
                val bakConfig = context.config
                val bakKeyword = context.keyword
                val bakKeywordOffset = context.keywordOffset
                val isKey = context.isKey
                context.config = cfg
                context.keyword = keywordToUse
                context.keywordOffset = innerStart
                context.isKey = null
                val resultToUse = result.withPrefixMatcher(keywordToUse)
                ParadoxCompletionManager.completeDefinition(context, resultToUse)
                context.keyword = bakKeyword
                context.keywordOffset = bakKeywordOffset
                context.config = bakConfig
                context.isKey = isKey
                return
            }
        }

        // otherwise, treat as localisation name
        run {
            fun isLocChar(ch: Char): Boolean {
                return ch.isLetterOrDigit() || ch == '_' || ch == '-' || ch == '.' || ch == '\''
            }

            var start = caret
            while (start > 0 && isLocChar(exprText[start - 1])) start--
            val keywordToUse = exprText.substring(start, caret)
            val cfg = CwtValueConfig.createMock(config.configGroup, "localisation")
            val bakConfig = context.config
            val bakKeyword = context.keyword
            val bakKeywordOffset = context.keywordOffset
            val isKey = context.isKey
            context.config = cfg
            context.keyword = keywordToUse
            context.keywordOffset = start
            context.isKey = null
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            ParadoxCompletionManager.completeLocalisation(context, resultToUse)
            context.keyword = bakKeyword
            context.keywordOffset = bakKeywordOffset
            context.config = bakConfig
            context.isKey = isKey
        }
    }

    private inline fun <T> markIncomplete(action: () -> T): T {
        return withState(PlsStates.incompleteComplexExpression, action)
    }

    private fun isIdentifierKeyword(context: ProcessingContext): Boolean {
        val keyword = context.keyword
        return keyword.isEmpty() || keyword.isIdentifier()
    }

    // endregion

    // region General Completion Methods

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForScopeLinkNode(node: ParadoxScopeLinkNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val oldArgIndex = context.argumentIndex
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeLinkNode = node.castOrNull<ParadoxDynamicScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val valueNode = scopeLinkNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordNode = currentArgNode ?: valueNode
            val keywordToUse = keywordNode.text.substring(0, offset - keywordNode.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = keywordNode.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            completeScopeLinkValue(context, resultToUse, prefixNode.text, currentArgNode)
            context.scopeContext = scopeContext
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            if (inFirstNode) {
                completeSystemScope(context, resultToUse)
                completeScope(context, resultToUse)
                completeScopeLinkPrefix(context, resultToUse)
            }
            completeScopeLinkValue(context, resultToUse, null, currentArgNode)
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.argumentIndex = oldArgIndex

        return inputPrefix
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForValueFieldNode(node: ParadoxValueFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val oldArgIndex = context.argumentIndex
        val fieldNode = node.castOrNull<ParadoxDynamicValueFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文

            val keywordNode = currentArgNode ?: valueNode
            val keywordToUse = keywordNode.text.substring(0, offset - keywordNode.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = keywordNode.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            completeValueFieldValue(context, resultToUse, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            if (inFirstNode) {
                completeValueField(context, resultToUse)
                completeValueFieldPrefix(context, resultToUse)
            }
            completeValueFieldValue(context, resultToUse, null, currentArgNode)
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.argumentIndex = oldArgIndex

        return inputPrefix
    }

    private fun completeForVariableFieldValueNode(node: ParadoxDataSourceNode, context: ProcessingContext, result: CompletionResultSet) {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
        val resultToUse = result.withPrefixMatcher(keywordToUse)
        context.keyword = keywordToUse
        context.keywordOffset = node.rangeInExpression.startOffset
        completeValueFieldValue(context, resultToUse, null, node, variableOnly = true)
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandScopeLinkNode(node: ParadoxCommandScopeLinkNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val oldArgIndex = context.argumentIndex
        val element = context.contextElement?.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeLinkNode = node.castOrNull<ParadoxDynamicCommandScopeLinkNode>()
        val prefixNode = scopeLinkNode?.prefixNode
        val valueNode = scopeLinkNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            context.scopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, scopeContext)

            val keywordNode = currentArgNode ?: valueNode
            val keywordToUse = keywordNode.text.substring(0, offset - keywordNode.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = keywordNode.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            completeCommandScopeLinkValue(context, resultToUse, prefixNode.text, currentArgNode)
            context.scopeContext = scopeContext
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            if (inFirstNode) {
                completeSystemScope(context, resultToUse)
                completeCommandScope(context, resultToUse)
                completeCommandScopeLinkPrefix(context, resultToUse)
            }
            completeCommandScopeLinkValue(context, resultToUse, null, currentArgNode)
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.argumentIndex = oldArgIndex

        return inputPrefix
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandFieldNode(node: ParadoxCommandFieldNode, context: ProcessingContext, result: CompletionResultSet): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val oldArgIndex = context.argumentIndex
        val fieldNode = node.castOrNull<ParadoxDynamicCommandFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文

            val keywordNode = currentArgNode ?: valueNode
            val keywordToUse = keywordNode.text.substring(0, offset - keywordNode.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = keywordNode.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            completeCommandFieldValue(context, resultToUse, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordToUse = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            context.keyword = keywordToUse
            context.keywordOffset = node.rangeInExpression.startOffset
            context.argumentIndex = argIndex
            if (inFirstNode) {
                completePredefinedCommandField(context, resultToUse)
                completeCommandField(context, resultToUse)
                completeCommandFieldPrefix(context, resultToUse)
            }
            completeCommandFieldValue(context, resultToUse, null, currentArgNode)
        }

        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.argumentIndex = oldArgIndex

        return inputPrefix
    }

    fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!

        // 总是提示，无论作用域是否匹配
        val systemScopeConfigs = configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            val name = systemScopeConfig.id
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withPriority(PlsCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScope(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linksConfigs = configGroup.linksModel.forScopeStatic
        for (linkConfig in linksConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.Scope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.linksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(PlsInsertHandlers.addParentheses())
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.linksModel.forScopeFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopeLinkValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        val argIndex = context.argumentIndex

        val linkConfigs = configGroup.links.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }
            .sortedByPriority({ it.configExpression }, { configGroup })
        context.config = null
        context.configs = linkConfigs

        when (argNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(linkConfigs, context, result)
        }

        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.argumentIndex = argIndex
    }

    fun completeValueField(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.linksModel.forValueStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            // 排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldPrefix(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.linksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(PlsInsertHandlers.addParentheses())
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.linksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?, variableOnly: Boolean = false) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        val argIndex = context.argumentIndex

        val linkConfigs = if (variableOnly) configGroup.linksModel.variable
        else configGroup.links.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }
            .sortedByPriority({ it.configExpression }, { configGroup })
        context.configs = linkConfigs

        when (argNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            is ParadoxScriptValueExpression -> completeScriptValueExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(linkConfigs, context, result)
        }

        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.argumentIndex = argIndex
    }

    fun completeDatabaseObjectType(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val tailText = " from database object types"
        val configs = configGroup.databaseObjectTypes.values
        for (config in configs) {
            val name = config.name
            val element = config.pointer.element ?: continue
            val typeFile = config.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.DatabaseObjectType)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(PlsCompletionPriorities.prefix)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeDatabaseObject(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val node = context.node?.castOrNull<ParadoxDatabaseObjectNode>()
            ?.nodes?.findIsInstance<ParadoxDatabaseObjectDataNode>()
            ?: return
        val config = node.config ?: return

        val typeToSearch = node.getTypeToSearch()
        if (typeToSearch == null) return

        context.expressionTailText = " from database object type ${config.name}"

        // complete forced base database object
        completeForcedBaseDatabaseObject(context, result, node)

        val extraFilter = f@{ e: PsiElement ->
            node.isValidDatabaseObject(e, typeToSearch)
        }
        val mockConfig = config.getConfigForType(node.isBase)
        val oldExtraFilter = context.extraFilter
        val oldConfig = context.config
        val oldTailText = context.expressionTailText
        context.extraFilter = extraFilter
        context.config = mockConfig
        if (config.localisation != null) {
            ParadoxCompletionManager.completeLocalisation(context, result)
        } else {
            ParadoxCompletionManager.completeDefinition(context, result)
        }
        context.extraFilter = oldExtraFilter
        context.config = oldConfig
        context.expressionTailText = oldTailText
    }

    private fun completeForcedBaseDatabaseObject(context: ProcessingContext, result: CompletionResultSet, dsNode: ParadoxDatabaseObjectDataNode) {
        val configGroup = context.configGroup!!
        val config = dsNode.config ?: return
        if (!dsNode.isPossibleForcedBase()) return
        val valueNode = dsNode.expression.valueNode ?: return
        val project = configGroup.project
        val contextElement = context.contextElement
        val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(valueNode.text, config.type, selector).processAsync {
            ParadoxCompletionManager.processDefinition(context, result, it)
        }
    }

    fun completeDefinePrefix(context: ProcessingContext, result: CompletionResultSet) {
        val name = "define:"
        val lookupElement = LookupElementBuilder.create(name)
            .withBoldness(true)
            .withPriority(PlsCompletionPriorities.prefix)
            .withCompletionId()
        result.addElement(lookupElement, context)
    }

    fun completeDefineNamespace(context: ProcessingContext, result: CompletionResultSet) {
        val project = context.parameters!!.originalFile.project
        val contextElement = context.contextElement
        val tailText = " from define namespaces"
        val selector = selector(project, contextElement).define().distinctByExpression()
        ParadoxDefineSearch.search(null, "", selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val namespace = info.namespace
            val element = ParadoxDefineManager.getDefineElement(info, project) ?: return@p true
            val lookupElement = LookupElementBuilder.create(element, namespace)
                .withPatchableIcon(PlsIcons.Nodes.DefineNamespace)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDefineVariable(context: ProcessingContext, result: CompletionResultSet) {
        val project = context.parameters!!.originalFile.project
        val contextElement = context.contextElement
        val node = context.node?.castOrNull<ParadoxDefineVariableNode>() ?: return
        val namespaceNode = node.expression.namespaceNode ?: return
        val namespace = namespaceNode.text
        val tailText = " from define namespace ${namespace}"
        val selector = selector(project, contextElement).define().distinctByExpression()
        ParadoxDefineSearch.search(namespace, null, selector).processAsync p@{ info ->
            ProgressManager.checkCanceled()
            val variable = info.variable ?: return@p true
            val element = ParadoxDefineManager.getDefineElement(info, project) ?: return@p true
            val lookupElement = LookupElementBuilder.create(element, variable)
                .withPatchableIcon(PlsIcons.Nodes.DefineVariable)
                .withPatchableTailText(tailText)
                .forScriptExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeCommandScope(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.localisationLinksModel.forScopeStatic
        for (linkConfig in linkConfigs) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            // optimize: make first char uppercase (e.g., owner -> Owner)
            val name = linkConfig.name.replaceFirstChar { it.uppercaseChar() }
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.LocalisationCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.localisationLinksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(PlsInsertHandlers.addParentheses())
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.localisationLinksModel.forScopeFromDataSorted
            .sortedByPriority({ it.configExpression }, { configGroup })
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeLinkValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        val argIndex = context.argumentIndex

        val linkConfigs = configGroup.localisationLinks.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }
            .sortedByPriority({ it.configExpression }, { configGroup })
        context.config = null
        context.configs = linkConfigs

        when (argNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(linkConfigs, context, result)
        }

        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.argumentIndex = argIndex
    }

    fun completePredefinedCommandField(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val localisationCommands = configGroup.localisationCommands
        for (localisationCommand in localisationCommands.values) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.LocalisationCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandField(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigs = configGroup.localisationLinksModel.forValueStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            // 排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.ValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldPrefix(context: ProcessingContext, result: CompletionResultSet) {
        if (!isIdentifierKeyword(context)) return // 前缀不合法时需要跳过，避免补全项被意外去重

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext

        val linkConfigsFromArgument = configGroup.localisationLinksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(PlsInsertHandlers.addParentheses())
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = configGroup.localisationLinksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            val scopeMatched = ParadoxScopeManager.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.Link)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(PlsCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldValue(context: ProcessingContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        val argIndex = context.argumentIndex

        val linkConfigs = configGroup.localisationLinks.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }
            .sortedByPriority({ it.configExpression }, { configGroup })
        context.config = null
        context.configs = linkConfigs

        when (argNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(linkConfigs, context, result)
        }

        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
        context.argumentIndex = argIndex
    }

    private fun completeScriptExpressionFromLinkConfigs(linkConfigs: List<CwtLinkConfig>, context: ProcessingContext, result: CompletionResultSet) {
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            context.config = linkConfig
            ParadoxCompletionManager.completeScriptExpression(context, result)
        }
    }

    // endregion
}
