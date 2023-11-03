package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

object PlsCompletionKeys : KeyRegistry

val PlsCompletionKeys.completionIds by createKey<MutableSet<String>>("paradoxCompletion.completionIds")
val PlsCompletionKeys.parameters by createKey<CompletionParameters>("paradoxCompletion.parameters")
val PlsCompletionKeys.contextElement by createKey<PsiElement>("paradoxCompletion.contextElement")
val PlsCompletionKeys.originalFile by createKey<PsiFile>("paradoxCompletion.originalFile")
val PlsCompletionKeys.quoted by createKey<Boolean>("paradoxCompletion.quoted") { false }
val PlsCompletionKeys.rightQuoted by createKey<Boolean>("paradoxCompletion.rightQuoted")
val PlsCompletionKeys.offsetInParent by createKey<Int>("paradoxCompletion.offsetInParent")
val PlsCompletionKeys.keyword by createKey<String>("paradoxCompletion.keyword") { "" }
val PlsCompletionKeys.keywordOffset by createKey<Int>("paradoxCompletion.keywordOffset") { 0 }
val PlsCompletionKeys.isKey by createKey<Boolean>("paradoxCompletion.isKey")
val PlsCompletionKeys.config by createKey<CwtConfig<*>>("paradoxCompletion.config")
val PlsCompletionKeys.configs by createKey<Collection<CwtConfig<*>>>("paradoxCompletion.configs") { emptyList() }
val PlsCompletionKeys.configGroup by createKey<CwtConfigGroup>("paradoxCompletion.configGroup")
val PlsCompletionKeys.scopeContext by createKey<ParadoxScopeContext>("paradoxCompletion.scopeContext")
val PlsCompletionKeys.scopeMatched by createKey<Boolean>("paradoxCompletion.scopeMatched") { true }
val PlsCompletionKeys.scopeName by createKey<String>("paradoxCompletion.scopeName")
val PlsCompletionKeys.scopeGroupName by createKey<String>("paradoxCompletion.scopeGroupName")
val PlsCompletionKeys.isInt by createKey<Boolean>("paradoxCompletion.isInt")

var ProcessingContext.completionIds by PlsCompletionKeys.completionIds
var ProcessingContext.parameters by PlsCompletionKeys.parameters
var ProcessingContext.contextElement by PlsCompletionKeys.contextElement
var ProcessingContext.originalFile by PlsCompletionKeys.originalFile
var ProcessingContext.quoted by PlsCompletionKeys.quoted
var ProcessingContext.rightQuoted by PlsCompletionKeys.rightQuoted
var ProcessingContext.offsetInParent by PlsCompletionKeys.offsetInParent
var ProcessingContext.keyword by PlsCompletionKeys.keyword
var ProcessingContext.keywordOffset by PlsCompletionKeys.keywordOffset
var ProcessingContext.isKey by PlsCompletionKeys.isKey
var ProcessingContext.config by PlsCompletionKeys.config
var ProcessingContext.configs by PlsCompletionKeys.configs
var ProcessingContext.configGroup by PlsCompletionKeys.configGroup
var ProcessingContext.scopeContext by PlsCompletionKeys.scopeContext
var ProcessingContext.scopeMatched by PlsCompletionKeys.scopeMatched
var ProcessingContext.scopeName by PlsCompletionKeys.scopeName
var ProcessingContext.scopeGroupName by PlsCompletionKeys.scopeGroupName
var ProcessingContext.isInt by PlsCompletionKeys.isInt