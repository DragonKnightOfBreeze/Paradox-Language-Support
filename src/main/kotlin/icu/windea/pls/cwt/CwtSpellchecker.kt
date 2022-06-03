package icu.windea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtSpellchecker : SpellcheckingStrategy() {
	companion object {
		private val textTokenizer = TEXT_TOKENIZER
		private val compactTextTokenizer = EMPTY_TOKENIZER
		private val emptyTokenizer = EMPTY_TOKENIZER
	}
	
	override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
		return when(element.elementType) {
			PROPERTY_KEY_TOKEN, OPTION_KEY_TOKEN -> compactTextTokenizer
			STRING_TOKEN -> compactTextTokenizer
			COMMENT, DOCUMENTATION_TOKEN -> textTokenizer
			else -> emptyTokenizer
		}
	}
}
