package com.windea.plugin.idea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*

class CwtSpellchecker:SpellcheckingStrategy() {
	override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
		return when(element.elementType){
			PROPERTY_KEY_TOKEN, OPTION_KEY_TOKEN -> TEXT_TOKENIZER
			STRING_TOKEN -> TEXT_TOKENIZER
			COMMENT, DOCUMENTATION_TOKEN -> TEXT_TOKENIZER
			else -> EMPTY_TOKENIZER
		}
	}
}
