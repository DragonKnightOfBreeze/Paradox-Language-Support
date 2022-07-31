package icu.windea.pls.script.codeInsight.completion

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*

object ParadoxDefinitionCompletionKeys {
	val contextElementKey = Key.create<PsiElement>("paradoxDefinitionCompletion.contextElement")
	val quotedKey = Key.create<Boolean>("paradoxDefinitionCompletion.quoted")
	val offsetInParentKey = Key.create<Int>("paradoxDefinitionCompletion.offsetInParent")
	val keywordKey = Key.create<String>("paradoxDefinitionCompletion.keyword")
	val isKeyKey = Key.create<Boolean>("paradoxDefinitionCompletion.isKey")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxDefinitionCompletion.configGroup")
}