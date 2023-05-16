package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.model.*

object PlsCompletionKeys {
	val completionIdsKey = Key.create<MutableSet<String>>("paradoxCompletion.completionIds")
	val parametersKey = Key.create<CompletionParameters>("paradoxCompletion.parameters")
	val contextElementKey = Key.create<PsiElement>("paradoxCompletion.contextElement")
	val originalFileKey = Key.create<PsiFile>("paradoxCompletion.originalFile")
	val quotedKey = Key.create<Boolean>("paradoxCompletion.quoted")
	val rightQuotedKey = Key.create<Boolean>("paradoxCompletion.rightQuoted")
	val offsetInParentKey = Key.create<Int>("paradoxCompletion.offsetInParent")
	val keywordKey = Key.create<String>("paradoxCompletion.keyword")
	val startOffsetKey = Key.create<Int>("paradoxCompletion.startOffset")
	val isKeyKey = Key.create<Boolean>("paradoxCompletion.isKey")
	val configKey = Key.create<CwtConfig<*>>("paradoxCompletion.config")
	val configsKey = Key.create<List<CwtConfig<*>>>("paradoxCompletion.configs")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxCompletion.configGroup")
	val scopeContextKey = Key.create<ParadoxScopeContext>("paradoxCompletion.scopeContext")
	val scopeMatchedKey = Key.create<Boolean>("paradoxCompletion.scopeMatched")
	val scopeNameKey = Key.create<String>("paradoxCompletion.scopeName")
	val scopeGroupNameKey = Key.create<String>("paradoxCompletion.scopeGroupName")
	val isIntKey = Key.create<Boolean>("paradoxCompletion.isInt")
}
