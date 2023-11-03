package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

object PlsCompletionKeys : KeyRegistry("PlsCompletion")

var ProcessingContext.completionIds: MutableSet<String>? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.parameters: CompletionParameters? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.contextElement: PsiElement? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.originalFile: PsiFile? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.quoted: Boolean by createKeyDelegate(PlsCompletionKeys) { false }
var ProcessingContext.rightQuoted: Boolean? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.offsetInParent: Int? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.keyword: String by createKeyDelegate(PlsCompletionKeys) { "" }
var ProcessingContext.keywordOffset: Int by createKeyDelegate(PlsCompletionKeys) { 0 }
var ProcessingContext.isKey: Boolean? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.config: CwtConfig<*>? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by createKeyDelegate(PlsCompletionKeys) { emptyList() }
var ProcessingContext.configGroup: CwtConfigGroup? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.scopeContext: ParadoxScopeContext? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.scopeMatched: Boolean by createKeyDelegate(PlsCompletionKeys) { true }
var ProcessingContext.scopeName: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.scopeGroupName: String? by createKeyDelegate(PlsCompletionKeys)
var ProcessingContext.isInt: Boolean? by createKeyDelegate(PlsCompletionKeys)