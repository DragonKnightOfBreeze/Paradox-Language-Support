package icu.windea.pls.localisation.psi

import com.intellij.lexer.*
import com.intellij.psi.tree.*
import icu.windea.pls.model.*

class ParadoxLocalisationLexer(
    val gameType: ParadoxGameType? = null
) : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer(gameType)), TOKENS_TO_MERGE) {
    companion object {
        private val TOKENS_TO_MERGE = TokenSet.create(
            ParadoxLocalisationElementTypes.STRING_TOKEN
        )
    }
}
