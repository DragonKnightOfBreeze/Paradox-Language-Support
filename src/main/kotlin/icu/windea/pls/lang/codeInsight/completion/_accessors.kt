package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScopeContext
import javax.swing.Icon

object PlsLookupElementKeys : KeyRegistry()

var LookupElement.completionId: String? by registerKey(PlsLookupElementKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by registerKey(PlsLookupElementKeys)

var LookupElement.priority: Double? by registerKey(PlsLookupElementKeys)
var LookupElement.patchableIcon: Icon? by registerKey(PlsLookupElementKeys)
var LookupElement.patchableTailText: String? by registerKey(PlsLookupElementKeys)
var LookupElement.localizedNames: Set<String>? by registerKey(PlsLookupElementKeys)
var LookupElement.scopeMatched: Boolean by registerKey(PlsLookupElementKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by registerKey(PlsLookupElementKeys) { false }

object PlsProcessingContextKeys : KeyRegistry()

var ProcessingContext.parameters: CompletionParameters? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.completionIds: MutableSet<String>? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.contextElement: PsiElement? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.offsetInParent: Int by registerKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.keyword: String by registerKey(PlsProcessingContextKeys) { "" }
var ProcessingContext.keywordOffset: Int by registerKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.quoted: Boolean by registerKey(PlsProcessingContextKeys) { false }
var ProcessingContext.rightQuoted: Boolean by registerKey(PlsProcessingContextKeys) { false }

var ProcessingContext.gameType: ParadoxGameType? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.configGroup: CwtConfigGroup? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.expressionOffset: Int by registerKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.extraFilter: ((PsiElement) -> Boolean)? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.isKey: Boolean? by registerKey(PlsProcessingContextKeys) // 如果是 `null`，则表示已经填充的只是键或值的其中一部分
var ProcessingContext.config: CwtConfig<*>? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by registerKey(PlsProcessingContextKeys) { emptyList() }
var ProcessingContext.scopeContext: ParadoxScopeContext? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.scopeMatched: Boolean by registerKey(PlsProcessingContextKeys) { true }
var ProcessingContext.scopeName: String? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.scopeGroupName: String? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.isInt: Boolean? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.prefix: String? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.expressionTailText: String? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.contextKey: String? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.argumentNames: MutableSet<String>? by registerKey(PlsProcessingContextKeys)
var ProcessingContext.node: ParadoxComplexExpressionNode? by registerKey(PlsProcessingContextKeys)

/** 在对多参数动态链接的代码补全中，表示当前光标所处的参数索引（从0开始）。*/
var ProcessingContext.argumentIndex: Int by registerKey(PlsProcessingContextKeys) { 0 }
