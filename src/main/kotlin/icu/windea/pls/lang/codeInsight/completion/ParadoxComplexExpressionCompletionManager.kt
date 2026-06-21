package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsIcons
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.prefixFromArgument
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.castOrNull
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
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.scriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.valueNode
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexExpressionCompletionManager {
    // region Entry Completion Methods

    fun completeTemplateExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val finalConfig = context.configs.firstOrNull() ?: context.config
        if (finalConfig == null) return

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxTemplateExpression.resolve(context.keyword, textRange, context.configGroup, finalConfig) } ?: return

        // skip check scope context here
        val context = context.copy(isKey = null, scopeContext = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxTemplateSnippetNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val config = node.config
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = config, configs = emptyList())
                    val result = result.withPrefixMatcher(context.keyword)
                    ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                    break
                }
            } else if (node is ParadoxTemplateSnippetConstantNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    // 一般来说，仅适用于是第一个节点的情况（否则，仍然会匹配范围内的通配符）
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val config = CwtValueConfig.createMock(context.configGroup, node.text)
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = config, configs = emptyList())
                    val result = result.withPrefixMatcher(context.keyword)
                    ParadoxExpressionCompletionManager.completeConstant(context, result)
                    break
                }
            }
        }
    }

    fun completeDynamicValueExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val finalConfigs = if (context.configs.isNotEmpty()) context.configs.toListOrThis() else context.config.to.singletonListOrEmpty()
        if (finalConfigs.isEmpty()) return

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDynamicValueExpression.resolve(context.keyword, textRange, context.configGroup, finalConfigs) } ?: return

        // skip check scope context here
        val context = context.copy(isKey = null, config = expression.configs.first(), configs = expression.configs, scopeContext = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDynamicValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset)
                    val result = result.withPrefixMatcher(context.keyword)
                    ParadoxExpressionCompletionManager.completeDynamicValue(context, result)
                    break
                }
            } else if (node is ParadoxScopeFieldExpression) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset)
                    val result = result.withPrefixMatcher(context.keyword)
                    completeScopeFieldExpression(context, result)
                    break
                }
            }
        }
    }

    fun completeScopeFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxScopeFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextInExpression = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    completeForScopeNode(context, result, node)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getScopeContext(element, node, scopeContextInExpression)
                }
            }
        }
    }

    fun completeScriptValueExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val config = context.config ?: return
        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxScriptValueExpression.resolve(context.keyword, textRange, context.configGroup, config) } ?: return

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        // skip check scope context here
        val context = context.copy(isKey = null, scopeContext = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxScriptValueNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val config = expression.config
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = config, configs = emptyList())
                    val result = result.withPrefixMatcher(context.keyword)
                    ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                }
            } else if (node is ParadoxScriptValueArgumentNode) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset)
                    val result = result.withPrefixMatcher(context.keyword)
                    ParadoxParameterManager.completeArguments(context, result, element)
                }
            } else if (node is ParadoxScriptValueArgumentValueNode && PlsSettings.getInstance().state.inference.configContextForParameters) {
                if (inRange && expression.scriptValueNode.text.isNotEmpty()) {
                    // 尝试提示传入参数的值
                    run {
                        val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                        val keywordOffset = node.rangeInExpression.startOffset
                        val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return@run
                        val inferredContextConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
                        val inferredConfig = inferredContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>() ?: return@run
                        val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = inferredConfig, configs = emptyList())
                        val result = result.withPrefixMatcher(context.keyword)
                        ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
                    }
                }
            }
        }
    }

    fun completeValueFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxValueFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextInExpression = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    completeForScopeNode(context, result, node)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getScopeContext(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxValueFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    val scopeNode = ParadoxScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForValueFieldNode(context, result, node)
                    break
                }
            }
        }
    }

    fun completeVariableFieldExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxVariableFieldExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextInExpression = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxScopeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    completeForScopeNode(context, result, node)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getScopeContext(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxDataSourceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    val scopeNode = ParadoxScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForVariableFieldValueNode(context, result, node)
                    break
                }
            }
        }
    }

    fun completeCommandExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()

        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxCommandExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        var scopeContextInExpression = scopeContext
        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (!inRange) {
                if (node is ParadoxErrorNode || node.text.isEmpty()) break // skip error or empty nodes
            }
            if (node is ParadoxCommandScopeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    completeForCommandScopeNode(context, result, node)
                    break
                } else {
                    scopeContextInExpression = ParadoxScopeManager.getScopeContext(element, node, scopeContextInExpression)
                }
            } else if (node is ParadoxCommandFieldNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val context = context.copy(scopeContext = scopeContextInExpression)
                    val scopeNode = ParadoxCommandScopeNode.resolve(node.text, node.rangeInExpression, context.configGroup)
                    val afterPrefix = completeForCommandScopeNode(context, result, scopeNode)
                    if (afterPrefix) break
                    completeForCommandFieldNode(context, result, node)
                    break
                }
            }
        }
    }

    fun completeDatabaseObjectExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDatabaseObjectExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDatabaseObjectTypeNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, node = node)
                    val result = result.withPrefixMatcher(context.keyword)
                    completeDatabaseObjectType(context, result)
                    break
                }
            } else if (node is ParadoxDatabaseObjectNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, node = node)
                    val result = result.withPrefixMatcher(keyword)
                    completeDatabaseObject(context, result)
                    break
                }
            }
        }
    }

    fun completeDefineReferenceExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val textRange = TextRange.from(context.keywordOffset, context.keyword.length)
        val expression = markIncomplete { ParadoxDefineReferenceExpression.resolve(context.keyword, textRange, context.configGroup) } ?: return

        val context = context.copy(isKey = null)
        for (node in expression.nodes) {
            ProgressManager.checkCanceled()
            if (node is ParadoxErrorNode && expression.nodes.size == 1) {
                ProgressManager.checkCanceled()
                completeDefinePrefix(context, result)
                break
            }
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if (node is ParadoxDefineNamespaceNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, node = node)
                    val result = result.withPrefixMatcher(context.keyword)
                    completeDefineNamespace(context, result)
                    break
                }
            } else if (node is ParadoxDefineVariableNode) {
                if (inRange) {
                    ProgressManager.checkCanceled()
                    val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
                    val keywordOffset = node.rangeInExpression.startOffset
                    val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, node = node)
                    val result = result.withPrefixMatcher(context.keyword)
                    completeDefineVariable(context, result)
                    break
                }
            }
        }
    }

    fun completeNameFormatExpression(context: ParadoxCompletionContext, result: CompletionResultSet) {
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
                val keyword = exprText.substring(innerStart, caret)
                val keywordOffset = innerStart
                val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, isKey = null)
                val result = result.withPrefixMatcher(context.keyword)
                ParadoxComplexExpressionCompletionManager.completeCommandExpression(context, result)
                return
            }
        }

        // inside <...>
        run {
            val leftAngle = lastUnclosedIndex('<', '>', caret)
            if (leftAngle >= 0) {
                val innerStart = leftAngle + 1
                val keyword = exprText.substring(innerStart, caret)
                val keywordOffset = innerStart
                val config = CwtValueConfig.createMock(config.configGroup, "<${type}>")
                val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = config, isKey = null)
                val result = result.withPrefixMatcher(context.keyword)
                ParadoxExpressionCompletionManager.completeDefinition(context, result)
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
            val keyword = exprText.substring(start, caret)
            val keywordOffset = start
            val config = CwtValueConfig.createMock(config.configGroup, "localisation")
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, config = config, isKey = null)
            val result = result.withPrefixMatcher(context.keyword)
            ParadoxExpressionCompletionManager.completeLocalisation(context, result)
        }
    }

    private inline fun <T> markIncomplete(action: () -> T): T {
        return withState(ChronicleThreadContext.incompleteComplexExpression, action)
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForScopeNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxScopeNode): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        val dynamicScopeNode = node.castOrNull<ParadoxDynamicScopeNode>()
        val prefixNode = dynamicScopeNode?.prefixNode
        val valueNode = dynamicScopeNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            val keywordNode = currentArgNode ?: valueNode
            val keywordOffset = keywordNode.rangeInExpression.startOffset
            val keyword = keywordNode.text.substring(0, offset - keywordOffset)
            val scopeContext = ParadoxScopeManager.getScopeContext(element, node, scopeContext)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, scopeContext = scopeContext, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            completeScopeValue(context, result, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
            val keywordOffset = node.rangeInExpression.startOffset
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(context.keyword)
            if (inFirstNode) {
                completeSystemScope(context, result)
                completeStaticScope(context, result)
                completeScopePrefix(context, result)
            }
            completeScopeValue(context, result, null, currentArgNode)
        }

        return inputPrefix
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForValueFieldNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxValueFieldNode): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val fieldNode = node.castOrNull<ParadoxDynamicValueFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文

            val keywordNode = currentArgNode ?: valueNode
            val keywordOffest = keywordNode.rangeInExpression.startOffset
            val keyword = keywordNode.text.substring(0, offset - keywordOffest)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffest, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            completeValueFieldValue(context, result, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordOffest = node.rangeInExpression.startOffset
            val keyword = node.text.substring(0, offset - keywordOffest)
            val result = result.withPrefixMatcher(keyword)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffest, linkArgIndex = argIndex)
            if (inFirstNode) {
                completeStaticValueField(context, result)
                completeValueFieldPrefix(context, result)
            }
            completeValueFieldValue(context, result, null, currentArgNode)
        }

        return inputPrefix
    }

    private fun completeForVariableFieldValueNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxDataSourceNode) {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return // unexpected

        val keyword = node.text.substring(0, offset - node.rangeInExpression.startOffset)
        val keywordOffset = node.rangeInExpression.startOffset
        val context = context.copy(keyword = keyword, keywordOffset = keywordOffset)
        val result = result.withPrefixMatcher(context.keyword)
        completeValueFieldValue(context, result, null, node, variableOnly = true)
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandScopeNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxCommandScopeNode): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val element = context.contextElement.castOrNull<ParadoxExpressionElement>() ?: return false
        val scopeContext = context.scopeContext ?: ParadoxScopeContext.resolveAny()
        val dynamicScopeNode = node.castOrNull<ParadoxDynamicCommandScopeNode>()
        val prefixNode = dynamicScopeNode?.prefixNode
        val valueNode = dynamicScopeNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            val keywordNode = currentArgNode ?: valueNode
            val keywordOffset = keywordNode.rangeInExpression.startOffset
            val keyword = node.text.substring(0, offset - keywordOffset)
            val scopeContext = ParadoxScopeManager.getScopeContext(element, node, scopeContext)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, scopeContext = scopeContext, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            completeCommandScopeValue(context, result, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty() || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordOffset = node.rangeInExpression.startOffset
            val keyword = node.text.substring(0, offset - keywordOffset)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            if (inFirstNode) {
                completeSystemCommandScope(context, result)
                completeStaticCommandScope(context, result)
                completeCommandScopePrefix(context, result)
            }
            completeCommandScopeValue(context, result, null, currentArgNode)
        }

        return inputPrefix
    }

    /**
     * @return 是否已经输入了前缀。
     */
    private fun completeForCommandFieldNode(context: ParadoxCompletionContext, result: CompletionResultSet, node: ParadoxCommandFieldNode): Boolean {
        val offset = context.offsetInParent - context.expressionOffset
        if (offset < 0) return false // unexpected

        var inputPrefix = false

        val fieldNode = node.castOrNull<ParadoxDynamicCommandFieldNode>()
        val prefixNode = fieldNode?.prefixNode
        val valueNode = fieldNode?.valueNode
        // locate argument node and index (prefer ParadoxLinkValueNode)
        val argIndex = valueNode?.getArgumentIndex(offset) ?: 0
        val currentArgNode = valueNode?.argumentNodes?.getOrNull(argIndex)
        if (prefixNode != null && valueNode != null && offset >= valueNode.rangeInExpression.startOffset) {
            // 不同于链接节点，这里没有必要切换作用域上下文

            val keywordNode = currentArgNode ?: valueNode
            val keywordOffset = keywordNode.rangeInExpression.startOffset
            val keyword = keywordNode.text.substring(0, offset - keywordOffset)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            completeCommandFieldValue(context, result, prefixNode.text, currentArgNode)
            inputPrefix = true
        } else {
            val inFirstNode = valueNode == null || valueNode.nodes.isEmpty()
                || offset <= valueNode.nodes.first().rangeInExpression.endOffset
            val keywordOffset = node.rangeInExpression.startOffset
            val keyword = node.text.substring(0, offset - keywordOffset)
            val context = context.copy(keyword = keyword, keywordOffset = keywordOffset, linkArgIndex = argIndex)
            val result = result.withPrefixMatcher(keyword)
            if (inFirstNode) {
                completeStaticCommandField(context, result)
                completeCommandFieldPrefix(context, result)
            }
            completeCommandFieldValue(context, result, null, currentArgNode)
        }

        return inputPrefix
    }

    // endregion

    // region General Completion Methods

    fun completeSystemScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        // 总是提示，无论作用域是否匹配
        val systemScopeConfigs = context.configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            ProgressManager.checkCanceled()
            val name = systemScopeConfig.id
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withPriority(ChronicleCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeStaticScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linksConfigs = context.configGroup.linksModel.forScopeStatic
        for (linkConfig in linksConfigs) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.StaticScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ChronicleCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopePrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.linksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(ChronicleInsertHandlers.addParentheses())
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.linksModel.forScopeFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeScopeValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.links.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    fun completeStaticValueField(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigs = context.configGroup.linksModel.forValueStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            // 排除 input_scopes 不匹配前一个 scope 的 output_scope 的情况
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.StaticValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldPrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.linksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(ChronicleInsertHandlers.addParentheses())
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.linksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicValueField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeValueFieldValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?, variableOnly: Boolean = false) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = if (variableOnly) context.configGroup.linksModel.variable
        else context.configGroup.links.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argNode) {
            is ParadoxDynamicValueExpression -> completeDynamicValueExpression(context, result)
            is ParadoxScopeFieldExpression -> completeScopeFieldExpression(context, result)
            is ParadoxValueFieldExpression -> completeValueFieldExpression(context, result)
            is ParadoxScriptValueExpression -> completeScriptValueExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    fun completeSystemCommandScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        // 总是提示，无论作用域是否匹配
        val systemScopeConfigs = context.configGroup.systemScopes
        for (systemScopeConfig in systemScopeConfigs.values) {
            ProgressManager.checkCanceled()
            val name = systemScopeConfig.id
            val element = systemScopeConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemScopeConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.SystemCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withPriority(ChronicleCompletionPriorities.systemScope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeStaticCommandScope(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigs = context.configGroup.localisationLinksModel.forScopeStatic
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            // optimize: make first char uppercase (e.g., owner -> Owner)
            val name = linkConfig.name.replaceFirstChar { it.uppercaseChar() }
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.StaticCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(ChronicleCompletionPriorities.scope)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopePrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.localisationLinksModel.forScopeFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(ChronicleInsertHandlers.addParentheses())
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.localisationLinksModel.forScopeFromDataSorted
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandScopeValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.localisationLinks.values.filter { it.type.forScope() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    fun completeStaticCommandField(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val localisationCommands = context.configGroup.localisationCommands
        for (localisationCommand in localisationCommands.values) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, localisationCommand.supportedScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.StaticCommandField)
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
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation links"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.StaticCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) // 忽略大小写
                .withScopeMatched(scopeMatched)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldPrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (!context.isIdentifierKeyword()) return // 前缀不合法时需要跳过，避免补全项被意外去重
        ProgressManager.checkCanceled()

        val linkConfigsFromArgument = context.configGroup.localisationLinksModel.forValueFromArgumentSorted
        for (linkConfig in linkConfigsFromArgument) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefixFromArgument ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = "(...) from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withInsertHandler(ChronicleInsertHandlers.addParentheses())
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }

        val linkConfigsFromData = context.configGroup.localisationLinksModel.forValueFromDataSorted
        for (linkConfig in linkConfigsFromData) {
            ProgressManager.checkCanceled()
            val scopeMatched = ParadoxScopeManager.matchesScope(context.scopeContext, linkConfig.inputScopes, context.configGroup)
            if (!scopeMatched && PlsSettings.getInstance().state.completion.completeOnlyScopeIsMatched) continue

            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from localisation link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withBoldness(true)
                .withIcon(PlsIcons.Nodes.DynamicCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withCompletionId()
            result.addElement(lookupElement, context)
        }
    }

    fun completeCommandFieldValue(context: ParadoxCompletionContext, result: CompletionResultSet, prefix: String?, argNode: ParadoxComplexExpressionNode?) {
        // NOTE 2.0.6 这里需要兼容多传参动态链接，支持正确地对其传参进行代码补全
        // NOTE 2.0.6 遇到单引号括起的字面量传参时，应中断代码补全（未来可能会完善这里的逻辑）

        if (argNode is ParadoxStringLiteralNode) return

        ProgressManager.checkCanceled()
        val linkConfigs = context.configGroup.localisationLinks.values.filter { it.type.forValue() && it.prefix == prefix }
            .mapNotNull { CwtLinkConfig.delegatedWith(it, context.linkArgIndex) }
            .sortedByPriority({ it.configExpression }, { context.configGroup })
        val context = context.copy(config = null, configs = linkConfigs)
        when (argNode) {
            is ParadoxCommandExpression -> completeCommandExpression(context, result)
            else -> completeScriptExpressionFromLinkConfigs(context, result, linkConfigs)
        }
    }

    fun completeScriptExpressionFromLinkConfigs(context: ParadoxCompletionContext, result: CompletionResultSet, linkConfigs: List<CwtLinkConfig>) {
        for (linkConfig in linkConfigs) {
            ProgressManager.checkCanceled()
            val context = context.copy(config = linkConfig)
            ParadoxExpressionCompletionManager.completeScriptExpression(context, result)
        }
    }

    fun completeDefinePrefix(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val name = "define:"
        val lookupElement = LookupElementBuilder.create(name)
            .withBoldness(true)
            .withPriority(ChronicleCompletionPriorities.keyword)
            .withCompletionId()
        result.addElement(lookupElement, context)
    }

    fun completeDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val project = context.parameters.originalFile.project
        val contextElement = context.contextElement
        val tailText = " from define namespaces"
        val selector = ParadoxDefineNamespaceSearch.selector(project, contextElement).distinct()
        ParadoxDefineNamespaceSearch.search(null, selector).processAsync p@{ element ->
            ProgressManager.checkCanceled()
            val defineInfo = element.defineNamespaceInfo ?: return@p true
            val namespace = defineInfo.namespace
            val lookupElement = LookupElementBuilder.create(element, namespace)
                .withPatchableIcon(PlsIcons.Nodes.DefineNamespace)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet) {
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
                .withPatchableIcon(PlsIcons.Nodes.DefineVariable)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
            true
        }
    }

    fun completeDatabaseObjectType(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val tailText = " from database object types"
        val configs = context.configGroup.databaseObjectTypes.values
        for (config in configs) {
            ProgressManager.checkCanceled()
            val name = config.name
            val element = config.pointer.element ?: continue
            val typeFile = config.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.DatabaseObjectType)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(ChronicleCompletionPriorities.prefix)
                .withPatchableTailText(tailText)
                .forExpression(context)
            result.addElement(lookupElement, context)
        }
    }

    fun completeDatabaseObject(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val node = context.node?.castOrNull<ParadoxDatabaseObjectNode>()
            ?.nodes?.findIsInstance<ParadoxDatabaseObjectDataNode>()
            ?: return
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

    fun completeForcedBaseDatabaseObject(context: ParadoxCompletionContext, result: CompletionResultSet, dsNode: ParadoxDatabaseObjectDataNode) {
        val config = dsNode.config ?: return
        if (!dsNode.isPossibleForcedBase()) return
        val valueNode = dsNode.expression.valueNode ?: return
        val selector = ParadoxDefinitionSearch.selector(context.project, context.contextElement).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchElement(valueNode.text, config.type, selector).processAsync {
            ParadoxCompletionUtil.processDefinition(context, result, it)
        }
    }

    // endregion
}
