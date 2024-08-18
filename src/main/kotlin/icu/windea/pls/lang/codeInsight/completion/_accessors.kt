package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import icu.windea.pls.lang.expression.complex.nodes.*
import javax.swing.Icon

object PlsCompletionKeys : KeyRegistry()

var ProcessingContext.parameters: CompletionParameters? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.completionIds: MutableSet<String>? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.contextElement: PsiElement? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.offsetInParent: Int? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.keyword: String by createKeyDelegate(PlsCompletionKeys) { "" }
var ProcessingContext.keywordOffset: Int by createKeyDelegate(PlsCompletionKeys) { 0 }
var ProcessingContext.quoted: Boolean by createKeyDelegate(PlsCompletionKeys) { false }
var ProcessingContext.rightQuoted: Boolean? by createKeyDelegate(PlsCompletionKeys)

var ProcessingContext.gameType: ParadoxGameType? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.configGroup: CwtConfigGroup? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.expressionOffset: Int by createKeyDelegate(PlsCompletionKeys) { 0 }
var ProcessingContext.extraFilter: ((PsiElement) -> Boolean)? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.isKey: Boolean? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.config: CwtConfig<*>? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by createKeyDelegate(PlsCompletionKeys) { emptyList() }
var ProcessingContext.scopeContext: ParadoxScopeContext? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.scopeMatched: Boolean by createKeyDelegate(PlsCompletionKeys) { true }
var ProcessingContext.scopeName: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.scopeGroupName: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.isInt: Boolean? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.prefix: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.expressionTailText: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.dataSourceNodeToCheck: ParadoxComplexExpressionNode? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.contextKey: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.argumentNames: MutableSet<String>? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.node: ParadoxComplexExpressionNode? by createKeyDelegate(PlsCompletionKeys)

var LookupElement.completionId: String? by createKeyDelegate(PlsCompletionKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by createKeyDelegate(PlsKeys)

var LookupElement.priority: Double? by createKeyDelegate(PlsCompletionKeys)
var LookupElement.patchableIcon: Icon? by createKeyDelegate(PlsCompletionKeys)
var LookupElement.patchableTailText: String? by createKeyDelegate(PlsCompletionKeys)
var LookupElement.localizedNames: Set<String>? by createKeyDelegate(PlsCompletionKeys)
var LookupElement.scopeMatched: Boolean by createKeyDelegate(PlsCompletionKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by createKeyDelegate(PlsCompletionKeys) { false }
