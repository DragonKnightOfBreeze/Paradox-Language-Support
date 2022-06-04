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
			VARIABLE_NAME_ID -> compactTextTokenizer
			PROPERTY_KEY_TOKEN -> compactTextTokenizer
			VARIABLE_REFERENCE_ID -> emptyTokenizer
			QUOTED_STRING_TOKEN -> compactTextTokenizer
			STRING_TOKEN -> compactTextTokenizer
			INPUT_PARAMETER_ID -> textTokenizer
			PARAMETER_ID -> textTokenizer
			COMMENT, END_OF_LINE_COMMENT -> textTokenizer
			else -> emptyTokenizer
		}
	}
}
