package icu.windea.pls.script.codeInsight.completion

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*

object ParadoxDefinitionCompletionKeys {
	val quotedKey = Key.create<Boolean>("paradoxDefinitionCompletion.quoted")
	val caretOffsetKey = Key.create<Int>("paradoxDefinitionCompletion.caretOffset")
	val keywordKey = Key.create<String>("paradoxDefinitionCompletion.keyword")
	val isKeyKey = Key.create<Boolean>("paradoxDefinitionCompletion.isKey")
	val configGroupKey = Key.create<CwtConfigGroup>("paradoxDefinitionCompletion.configGroup")
}