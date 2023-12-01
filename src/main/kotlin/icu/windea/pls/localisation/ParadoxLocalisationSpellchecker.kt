package icu.windea.pls.localisation

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = TEXT_TOKENIZER
    private val compactTextTokenizer = EMPTY_TOKENIZER
    private val emptyTokenizer = EMPTY_TOKENIZER
    
    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        return when(element.elementType) {
            LOCALE_TOKEN -> emptyTokenizer
            PROPERTY_KEY_TOKEN -> compactTextTokenizer
            PROPERTY_REFERENCE_TOKEN -> emptyTokenizer
            COMMAND_SCOPE, COMMAND_FIELD, ICON_TOKEN -> textTokenizer
            PLAIN_TEXT_TOKEN -> textTokenizer
            COMMENT -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
