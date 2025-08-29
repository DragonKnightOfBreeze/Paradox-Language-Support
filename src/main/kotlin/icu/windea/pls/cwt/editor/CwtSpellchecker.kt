package icu.windea.pls.cwt.editor

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import icu.windea.pls.cwt.psi.CwtElementTypes

class CwtSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = TEXT_TOKENIZER
    private val compactTextTokenizer = EMPTY_TOKENIZER
    private val emptyTokenizer = EMPTY_TOKENIZER

    override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
        return when (element.elementType) {
            CwtElementTypes.PROPERTY_KEY_TOKEN, CwtElementTypes.OPTION_KEY_TOKEN -> compactTextTokenizer
            CwtElementTypes.STRING_TOKEN -> compactTextTokenizer
            CwtElementTypes.COMMENT, CwtElementTypes.DOC_COMMENT_TOKEN -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
