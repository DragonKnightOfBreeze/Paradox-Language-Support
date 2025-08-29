package icu.windea.pls.csv.editor

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes

class ParadoxCsvSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = TEXT_TOKENIZER
    private val emptyTokenizer = EMPTY_TOKENIZER

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        return when (element.elementType) {
            ParadoxCsvElementTypes.COLUMN_TOKEN -> textTokenizer
            ParadoxCsvElementTypes.COMMENT -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
