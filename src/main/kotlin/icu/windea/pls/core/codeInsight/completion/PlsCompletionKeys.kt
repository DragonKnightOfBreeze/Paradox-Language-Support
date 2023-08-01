package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

object PlsCompletionKeys

val PlsCompletionKeys.completionIds by lazy { keyOf<MutableSet<String>>("paradoxCompletion.completionIds") }
val PlsCompletionKeys.parameters by lazy { keyOf<CompletionParameters>("paradoxCompletion.parameters") }
val PlsCompletionKeys.contextElement by lazy { keyOf<PsiElement>("paradoxCompletion.contextElement") }
val PlsCompletionKeys.originalFile by lazy { keyOf<PsiFile>("paradoxCompletion.originalFile") }
val PlsCompletionKeys.quoted by lazy { keyOf<Boolean>("paradoxCompletion.quoted") { false } }
val PlsCompletionKeys.rightQuoted by lazy { keyOf<Boolean>("paradoxCompletion.rightQuoted") }
val PlsCompletionKeys.offsetInParent by lazy { keyOf<Int>("paradoxCompletion.offsetInParent") }
val PlsCompletionKeys.keyword by lazy { keyOf<String>("paradoxCompletion.keyword") }
val PlsCompletionKeys.startOffset by lazy { keyOf<Int>("paradoxCompletion.startOffset") }
val PlsCompletionKeys.isKey by lazy { keyOf<Boolean>("paradoxCompletion.isKey") }
val PlsCompletionKeys.config by lazy { keyOf<CwtConfig<*>>("paradoxCompletion.config") }
val PlsCompletionKeys.configs by lazy { keyOf<Collection<CwtConfig<*>>>("paradoxCompletion.configs") }
val PlsCompletionKeys.configGroup by lazy { keyOf<CwtConfigGroup>("paradoxCompletion.configGroup") }
val PlsCompletionKeys.scopeContext by lazy { keyOf<ParadoxScopeContext>("paradoxCompletion.scopeContext") }
val PlsCompletionKeys.scopeMatched by lazy { keyOf<Boolean>("paradoxCompletion.scopeMatched") { true } }
val PlsCompletionKeys.scopeName by lazy { keyOf<String>("paradoxCompletion.scopeName") }
val PlsCompletionKeys.scopeGroupName by lazy { keyOf<String>("paradoxCompletion.scopeGroupName") }
val PlsCompletionKeys.isInt by lazy { keyOf<Boolean>("paradoxCompletion.isInt") }

var ProcessingContext.completionIds by PlsCompletionKeys.completionIds
var ProcessingContext.parameters by PlsCompletionKeys.parameters
var ProcessingContext.contextElement by PlsCompletionKeys.contextElement
var ProcessingContext.originalFile by PlsCompletionKeys.originalFile
var ProcessingContext.quoted by PlsCompletionKeys.quoted
var ProcessingContext.rightQuoted by PlsCompletionKeys.rightQuoted
var ProcessingContext.offsetInParent by PlsCompletionKeys.offsetInParent
var ProcessingContext.keyword by PlsCompletionKeys.keyword
var ProcessingContext.startOffset by PlsCompletionKeys.startOffset
var ProcessingContext.isKey: Boolean? by PlsCompletionKeys.isKey
var ProcessingContext.config by PlsCompletionKeys.config
var ProcessingContext.configs by PlsCompletionKeys.configs
var ProcessingContext.configGroup by PlsCompletionKeys.configGroup
var ProcessingContext.scopeContext by PlsCompletionKeys.scopeContext
var ProcessingContext.scopeMatched by PlsCompletionKeys.scopeMatched
var ProcessingContext.scopeName by PlsCompletionKeys.scopeName
var ProcessingContext.scopeGroupName by PlsCompletionKeys.scopeGroupName
var ProcessingContext.isInt by PlsCompletionKeys.isInt