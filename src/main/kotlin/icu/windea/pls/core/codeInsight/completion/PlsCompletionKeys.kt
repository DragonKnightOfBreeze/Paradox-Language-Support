package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*

object PlsCompletionKeys {
	val completionTypeKey = Key.create<CompletionType>("paradoxCompletion.completionType")
	val contextElementKey = Key.create<PsiElement>("paradoxCompletion.contextElement")
	val quotedKey = Key.create<Boolean>("paradoxCompletion.quoted")
	val offsetInParentKey = Key.create<Int>("paradoxCompletion.offsetInParent")
	val keywordKey = Key.create<String>("paradoxCompletion.keyword")
	val isKeyKey = Key.create<Boolean>("paradoxCompletion.isKey")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxCompletion.configGroup")
	val prevScopeKey = Key.create<String>("paradoxCompletion.prevScope")
	val isExpectedScopeMatchedKey = Key.create<Boolean>("paradoxCompletion.isScopeMatched")
	val scopeNameKey = Key.create<String>("paradoxCompletion.scopeName")
	val scopeGroupNameKey = Key.create<String>("paradoxCompletion.scopeGroupName")
	val isIntKey = Key.create<Boolean>("paradoxCompletion.isInt")
}

val ProcessingContext.completionType get() = get(PlsCompletionKeys.completionTypeKey)
val ProcessingContext.contextElement get() = get(PlsCompletionKeys.contextElementKey)
val ProcessingContext.quoted get() = get(PlsCompletionKeys.quotedKey)
val ProcessingContext.offsetInParent get() = get(PlsCompletionKeys.offsetInParentKey)
val ProcessingContext.keyword get() = get(PlsCompletionKeys.keywordKey)
val ProcessingContext.isKey get() = get(PlsCompletionKeys.isKeyKey)
val ProcessingContext.configGroup get() = get(PlsCompletionKeys.configGroupKey)
val ProcessingContext.prevScope get() = get(PlsCompletionKeys.prevScopeKey)
val ProcessingContext.isExpectedScopeMatched get() = get(PlsCompletionKeys.isExpectedScopeMatchedKey) ?: true
val ProcessingContext.scopeName get() = get(PlsCompletionKeys.scopeNameKey)
val ProcessingContext.scopeGroupName get() = get(PlsCompletionKeys.scopeGroupNameKey)
val ProcessingContext.isInt get() = get(PlsCompletionKeys.isIntKey) ?: false