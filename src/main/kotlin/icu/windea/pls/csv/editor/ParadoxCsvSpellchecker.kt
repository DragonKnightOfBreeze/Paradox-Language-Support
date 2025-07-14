package icu.windea.pls.csv.editor

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.csv.psi.*

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
