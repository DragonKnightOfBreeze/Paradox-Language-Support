package icu.windea.pls.localisation

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationSpellchecker : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<*> {
		return when(element.elementType) {
			//PROPERTY_KEY_ID, COMMAND_SCOPE, COMMAND_FIELD, ICON_ID -> TEXT_TOKENIZER
			STRING_TOKEN -> TEXT_TOKENIZER
			COMMENT, END_OF_LINE_COMMENT -> TEXT_TOKENIZER
			else -> super.getTokenizer(element)
		}
	}
}
