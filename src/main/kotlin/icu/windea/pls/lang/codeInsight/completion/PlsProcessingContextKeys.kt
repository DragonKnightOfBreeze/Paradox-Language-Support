package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.expression.complex.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxScopeContext

object PlsProcessingContextKeys : KeyRegistry()

var ProcessingContext.parameters: CompletionParameters? by createKey(PlsProcessingContextKeys)
var ProcessingContext.completionIds: MutableSet<String>? by createKey(PlsProcessingContextKeys)
var ProcessingContext.contextElement: PsiElement? by createKey(PlsProcessingContextKeys)
var ProcessingContext.offsetInParent: Int by createKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.keyword: String by createKey(PlsProcessingContextKeys) { "" }
var ProcessingContext.keywordOffset: Int by createKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.quoted: Boolean by createKey(PlsProcessingContextKeys) { false }
var ProcessingContext.rightQuoted: Boolean by createKey(PlsProcessingContextKeys) { false }

var ProcessingContext.gameType: ParadoxGameType? by createKey(PlsProcessingContextKeys)
var ProcessingContext.configGroup: CwtConfigGroup? by createKey(PlsProcessingContextKeys)
var ProcessingContext.expressionOffset: Int by createKey(PlsProcessingContextKeys) { 0 }
var ProcessingContext.extraFilter: ((PsiElement) -> Boolean)? by createKey(PlsProcessingContextKeys)
var ProcessingContext.isKey: Boolean? by createKey(PlsProcessingContextKeys)
var ProcessingContext.config: CwtConfig<*>? by createKey(PlsProcessingContextKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by createKey(PlsProcessingContextKeys) { emptyList() }
var ProcessingContext.scopeContext: ParadoxScopeContext? by createKey(PlsProcessingContextKeys)
var ProcessingContext.scopeMatched: Boolean by createKey(PlsProcessingContextKeys) { true }
var ProcessingContext.scopeName: String? by createKey(PlsProcessingContextKeys)
var ProcessingContext.scopeGroupName: String? by createKey(PlsProcessingContextKeys)
var ProcessingContext.isInt: Boolean? by createKey(PlsProcessingContextKeys)
var ProcessingContext.prefix: String? by createKey(PlsProcessingContextKeys)
var ProcessingContext.expressionTailText: String? by createKey(PlsProcessingContextKeys)
var ProcessingContext.contextKey: String? by createKey(PlsProcessingContextKeys)
var ProcessingContext.argumentNames: MutableSet<String>? by createKey(PlsProcessingContextKeys)
var ProcessingContext.node: ParadoxComplexExpressionNode? by createKey(PlsProcessingContextKeys)
