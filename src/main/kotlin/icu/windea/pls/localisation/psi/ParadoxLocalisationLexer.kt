package icu.windea.pls.localisation.psi

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class ParadoxLocalisationLexer : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer()), TOKENS_TO_MERGE) {
    companion object {
        private val TOKENS_TO_MERGE = TokenSet.EMPTY
    }
}