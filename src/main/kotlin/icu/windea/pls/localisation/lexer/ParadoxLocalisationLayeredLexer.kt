package icu.windea.pls.localisation.lexer

import com.intellij.lexer.LayeredLexer
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationLayeredLexer(
    val gameType: ParadoxGameType? = null
): LayeredLexer(ParadoxLocalisationLexer()) {
    init {
        ParadoxLocalisationLexerFactory.registerTextLexer(this, gameType)
    }
}
