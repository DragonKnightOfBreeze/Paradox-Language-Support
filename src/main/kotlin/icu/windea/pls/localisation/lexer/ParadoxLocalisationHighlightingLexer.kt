package icu.windea.pls.localisation.lexer

import com.intellij.lexer.LayeredLexer
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationHighlightingLexer(
    val gameType: ParadoxGameType? = null
) : LayeredLexer(ParadoxLocalisationLexer()) {
    init {
        ParadoxLocalisationLexerFactory.registerTextLexerWithLiteralLexer(this, gameType)
    }
}
