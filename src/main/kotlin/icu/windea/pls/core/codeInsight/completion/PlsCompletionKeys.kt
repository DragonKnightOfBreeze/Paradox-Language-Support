package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*

object PlsCompletionKeys {
	val completionTypeKey = Key.create<CompletionType>("paradoxCompletion.completionType")
	val contextElementKey = Key.create<PsiElement>("paradoxCompletion.contextElement")
	val quotedKey = Key.create<Boolean>("paradoxCompletion.quoted")
	val offsetInParentKey = Key.create<Int>("paradoxCompletion.offsetInParent")
	val keywordKey = Key.create<String>("paradoxCompletion.keyword")
	val isKeyKey = Key.create<Boolean>("paradoxCompletion.isKey")
	val configKey = Key.create<CwtConfig<*>>("paradoxCompletion.config")
	val configsKey = Key.create<List<CwtConfig<*>>>("paradoxCompletion.configs")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxCompletion.configGroup")
	val prevScopeKey = Key.create<String>("paradoxCompletion.prevScope")
	val isExpectedScopeMatchedKey = Key.create<Boolean>("paradoxCompletion.isScopeMatched")
	val scopeNameKey = Key.create<String>("paradoxCompletion.scopeName")
	val scopeGroupNameKey = Key.create<String>("paradoxCompletion.scopeGroupName")
	val isIntKey = Key.create<Boolean>("paradoxCompletion.isInt")
	val valueSetName = Key.create<String>("paradoxCompletion.valueSetName")
}
