package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*

object PlsCompletionKeys {
	val contextElementKey = Key.create<PsiElement>("paradoxDefinitionCompletion.contextElement")
	val quotedKey = Key.create<Boolean>("paradoxDefinitionCompletion.quoted")
	val offsetInParentKey = Key.create<Int>("paradoxDefinitionCompletion.offsetInParent")
	val keywordKey = Key.create<String>("paradoxDefinitionCompletion.keyword")
	val isKeyKey = Key.create<Boolean>("paradoxDefinitionCompletion.isKey")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxDefinitionCompletion.configGroup")
	val prevScopeKey = Key.create<String>("paradoxDefinitionCompletion.prevScope")
}

val ProcessingContext.contextElement get() = get(PlsCompletionKeys.contextElementKey)
val ProcessingContext.quoted get() = get(PlsCompletionKeys.quotedKey)
val ProcessingContext.offsetInParent get() = get(PlsCompletionKeys.offsetInParentKey)
val ProcessingContext.keyword get() = get(PlsCompletionKeys.keywordKey)
val ProcessingContext.isKey get() = get(PlsCompletionKeys.isKeyKey)
val ProcessingContext.configGroup get() = get(PlsCompletionKeys.configGroupKey)
val ProcessingContext.prevScope get() = get(PlsCompletionKeys.prevScopeKey)