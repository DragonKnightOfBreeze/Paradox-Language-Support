package icu.windea.pls.localisation

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*

//拼写检查：
//检查key、value、comment

class ParadoxLocalisationSpellchecker : SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement): Tokenizer<*> {
		return when(element.elementType) {
			PROPERTY_KEY_ID,COMMAND_SCOPE,COMMAND_FIELD, ICON_ID -> TEXT_TOKENIZER
			STRING_TOKEN -> TEXT_TOKENIZER
			COMMENT,ROOT_COMMENT,END_OF_LINE_COMMENT -> TEXT_TOKENIZER
			else -> EMPTY_TOKENIZER
		}
	}
}
