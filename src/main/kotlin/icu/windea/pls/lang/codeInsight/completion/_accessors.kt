package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*

object ParadoxCompletionKeys : KeyRegistry("PlsCompletion")

var ProcessingContext.parameters: CompletionParameters? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.completionIds: MutableSet<String>? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.gameType: ParadoxGameType? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.configGroup: CwtConfigGroup? by createKeyDelegate(ParadoxCompletionKeys)

var ProcessingContext.contextElement: PsiElement? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.quoted: Boolean by createKeyDelegate(ParadoxCompletionKeys) { false }
var ProcessingContext.rightQuoted: Boolean? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.offsetInParent: Int? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.keyword: String by createKeyDelegate(ParadoxCompletionKeys) { "" }
var ProcessingContext.keywordOffset: Int by createKeyDelegate(ParadoxCompletionKeys) { 0 }
var ProcessingContext.isKey: Boolean? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.config: CwtConfig<*>? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.configs: Collection<CwtConfig<*>> by createKeyDelegate(ParadoxCompletionKeys) { emptyList() }
var ProcessingContext.scopeContext: ParadoxScopeContext? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.scopeMatched: Boolean by createKeyDelegate(ParadoxCompletionKeys) { true }
var ProcessingContext.scopeName: String? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.scopeGroupName: String? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.isInt: Boolean? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.prefix: String? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.dataSourceNodeToCheck: ParadoxComplexExpressionNode? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.showScriptExpressionTailText: Boolean by createKeyDelegate(ParadoxCompletionKeys) { true }
var ProcessingContext.contextKey: String? by createKeyDelegate(ParadoxCompletionKeys)
var ProcessingContext.argumentNames: MutableSet<String>? by createKeyDelegate(ParadoxCompletionKeys)

fun ProcessingContext.initialize(parameters: CompletionParameters) {
    this.parameters = parameters
    this.completionIds = mutableSetOf<String>().synced()
    
    val gameType = selectGameType(parameters.originalFile)
    this.gameType = gameType
    
    if(gameType != null) {
        val project = parameters.originalFile.project
        val configGroup = getConfigGroup(project, gameType)
        this.configGroup = configGroup
    }
}
