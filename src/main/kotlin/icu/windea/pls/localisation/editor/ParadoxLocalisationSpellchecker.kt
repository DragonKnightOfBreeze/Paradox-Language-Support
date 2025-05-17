package icu.windea.pls.localisation.editor

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = TEXT_TOKENIZER
    private val compactTextTokenizer = EMPTY_TOKENIZER
    private val emptyTokenizer = EMPTY_TOKENIZER

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        return when (element.elementType) {
            ParadoxLocalisationElementTypes.LOCALE_TOKEN -> emptyTokenizer
            ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN -> compactTextTokenizer
            ParadoxLocalisationElementTypes.PROPERTY_REFERENCE_TOKEN -> emptyTokenizer
            ParadoxLocalisationElementTypes.ICON_TOKEN -> textTokenizer
            ParadoxLocalisationElementTypes.STRING_TOKEN -> textTokenizer
            ParadoxLocalisationElementTypes.COMMAND_TEXT_TOKEN -> textTokenizer
            ParadoxLocalisationElementTypes.CONCEPT_NAME_TOKEN -> textTokenizer
            ParadoxLocalisationElementTypes.COMMENT -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
