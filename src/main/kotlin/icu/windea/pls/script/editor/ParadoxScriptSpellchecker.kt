package icu.windea.pls.script.editor

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.spellchecker.tokenizer.*
import icu.windea.pls.script.psi.*

class ParadoxScriptSpellchecker : SpellcheckingStrategy() {
    private val textTokenizer = TEXT_TOKENIZER
    private val compactTextTokenizer = EMPTY_TOKENIZER
    private val emptyTokenizer = EMPTY_TOKENIZER

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        return when (element.elementType) {
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN -> textTokenizer
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN -> textTokenizer
            ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN -> compactTextTokenizer
            ParadoxScriptElementTypes.STRING_TOKEN -> compactTextTokenizer
            ParadoxScriptElementTypes.CONDITION_PARAMETER_TOKEN -> textTokenizer
            ParadoxScriptElementTypes.PARAMETER_TOKEN -> textTokenizer
            ParadoxScriptElementTypes.COMMENT -> textTokenizer
            else -> emptyTokenizer
        }
    }
}
