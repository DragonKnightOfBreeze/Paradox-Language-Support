package com.windea.plugin.idea.paradox.script

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

//拼写检查：
//检查variableName、key、value、comment

class ParadoxScriptSpellchecker : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<*> {
		return when(element.elementType) {
			VARIABLE_NAME_ID -> TEXT_TOKENIZER
			PROPERTY_KEY_ID -> TEXT_TOKENIZER
			VARIABLE_REFERENCE_ID -> TEXT_TOKENIZER
			QUOTED_STRING_TOKEN -> TEXT_TOKENIZER
			STRING_TOKEN -> TEXT_TOKENIZER
			COMMENT -> TEXT_TOKENIZER
			END_OF_LINE_COMMENT -> TEXT_TOKENIZER
			else -> EMPTY_TOKENIZER
		}
	}
}
