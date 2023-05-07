package icu.windea.pls.script

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptSpellchecker : SpellcheckingStrategy() {
	companion object{
		private val textTokenizer = TEXT_TOKENIZER
		private val compactTextTokenizer = EMPTY_TOKENIZER
		private val emptyTokenizer = EMPTY_TOKENIZER
	}
	
	override fun getTokenizer(element: PsiElement): Tokenizer<*> {
		return when(element.elementType) {
			SCRIPTED_VARIABLE_NAME_TOKEN -> textTokenizer
			SCRIPTED_VARIABLE_REFERENCE_TOKEN -> textTokenizer
			QUOTED_PROPERTY_KEY_TOKEN -> textTokenizer
			PROPERTY_KEY_TOKEN -> compactTextTokenizer
			QUOTED_STRING_TOKEN -> textTokenizer
			STRING_TOKEN, STRING_SNIPPET -> compactTextTokenizer
			CONDITION_PARAMETER_TOKEN -> textTokenizer
			PARAMETER_TOKEN-> textTokenizer
			COMMENT -> textTokenizer
			else -> emptyTokenizer
		}
	}
}
