package icu.windea.pls.script.psi

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class ParadoxScriptLexer : MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer()), TOKENS_TO_MERGE) {
    companion object {
        private val TOKENS_TO_MERGE = TokenSet.create(
            ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN,
            ParadoxScriptElementTypes.STRING_TOKEN,
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN,
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
        )
    }
}