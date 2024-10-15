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
        return when (element.elementType) {
            LOCALE_TOKEN -> emptyTokenizer
            PROPERTY_KEY_TOKEN -> compactTextTokenizer
            PROPERTY_REFERENCE_TOKEN -> emptyTokenizer
            ICON_TOKEN -> textTokenizer
            STRING_TOKEN -> textTokenizer
            COMMAND_TEXT_TOKEN -> textTokenizer
            CONCEPT_NAME_TOKEN -> textTokenizer
            COMMENT -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
