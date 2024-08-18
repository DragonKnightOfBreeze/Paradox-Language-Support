package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.model.*
import javax.swing.*

object PlsProcessingContextKeys : KeyRegistry()

var ProcessingContext.parameters: CompletionParameters? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.completionIds: MutableSet<String>? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.contextElement: PsiElement? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.offsetInParent: Int? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.keyword: String by createKeyDelegate(PlsProcessingContextKeys) { "" }
var ProcessingContext.keywordOffset: Int by createKeyDelegate(PlsProcessingContextKeys) { 0 }
var ProcessingContext.quoted: Boolean by createKeyDelegate(PlsProcessingContextKeys) { false }
var ProcessingContext.rightQuoted: Boolean by createKeyDelegate(PlsProcessingContextKeys) { false }

var ProcessingContext.gameType: ParadoxGameType? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.configGroup: CwtConfigGroup? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.expressionOffset: Int by createKeyDelegate(PlsProcessingContextKeys) { 0 }
var ProcessingContext.extraFilter: ((PsiElement) -> Boolean)? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.isKey: Boolean? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.config: CwtConfig<*>? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by createKeyDelegate(PlsProcessingContextKeys) { emptyList() }
var ProcessingContext.scopeContext: ParadoxScopeContext? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.scopeMatched: Boolean by createKeyDelegate(PlsProcessingContextKeys) { true }
var ProcessingContext.scopeName: String? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.scopeGroupName: String? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.isInt: Boolean? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.prefix: String? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.expressionTailText: String? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.dataSourceNodeToCheck: ParadoxComplexExpressionNode? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.contextKey: String? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.argumentNames: MutableSet<String>? by createKeyDelegate(PlsProcessingContextKeys)
var ProcessingContext.node: ParadoxComplexExpressionNode? by createKeyDelegate(PlsProcessingContextKeys)

object PlsLookupElementKeys : KeyRegistry()

var LookupElement.completionId: String? by createKeyDelegate(PlsLookupElementKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by createKeyDelegate(PlsLookupElementKeys)

var LookupElement.priority: Double? by createKeyDelegate(PlsLookupElementKeys)
var LookupElement.patchableIcon: Icon? by createKeyDelegate(PlsLookupElementKeys)
var LookupElement.patchableTailText: String? by createKeyDelegate(PlsLookupElementKeys)
var LookupElement.localizedNames: Set<String>? by createKeyDelegate(PlsLookupElementKeys)
var LookupElement.scopeMatched: Boolean by createKeyDelegate(PlsLookupElementKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by createKeyDelegate(PlsLookupElementKeys) { false }
