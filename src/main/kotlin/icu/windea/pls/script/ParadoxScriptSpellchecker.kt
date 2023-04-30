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
			SCRIPTED_VARIABLE_NAME_TOKEN -> compactTextTokenizer
			PROPERTY_KEY_TOKEN -> compactTextTokenizer
			SCRIPTED_VARIABLE_REFERENCE_TOKEN -> emptyTokenizer
			QUOTED_STRING_TOKEN -> compactTextTokenizer
			STRING_TOKEN -> compactTextTokenizer
			ARGUMENT_ID -> textTokenizer
			KEY_PARAMETER_TOKEN, VALUE_PARAMETER_TOKEN, INLINE_MATH_PARAMETER_TOKEN -> textTokenizer
			COMMENT -> textTokenizer
			else -> emptyTokenizer
		}
	}
}
