package icu.windea.pls.localisation

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationSpellchecker : SpellcheckingStrategy() {
	companion object {
		private val textTokenizer = TEXT_TOKENIZER
		private val emptyTokenizer = EMPTY_TOKENIZER
	}
	
	override fun getTokenizer(element: PsiElement): Tokenizer<*> {
		return when(element.elementType) {
			LOCALE_ID -> emptyTokenizer
			PROPERTY_KEY_ID -> textTokenizer
			PROPERTY_REFERENCE_ID -> emptyTokenizer
			COMMAND_SCOPE, COMMAND_FIELD, ICON_ID -> textTokenizer
			STRING_TOKEN -> textTokenizer
			COMMENT, END_OF_LINE_COMMENT -> textTokenizer
			else -> emptyTokenizer
		}
	}
}
