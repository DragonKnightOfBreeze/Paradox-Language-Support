package icu.windea.pls.cwt

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = SpellcheckingStrategy.TEXT_TOKENIZER
    private val compactTextTokenizer = SpellcheckingStrategy.EMPTY_TOKENIZER
    private val emptyTokenizer = SpellcheckingStrategy.EMPTY_TOKENIZER
    
    override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
        return when(element.elementType) {
            PROPERTY_KEY_TOKEN, OPTION_KEY_TOKEN -> compactTextTokenizer
            STRING_TOKEN -> compactTextTokenizer
            COMMENT, DOCUMENTATION_TOKEN -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
