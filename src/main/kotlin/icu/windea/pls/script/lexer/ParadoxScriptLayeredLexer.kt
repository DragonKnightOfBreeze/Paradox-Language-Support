package icu.windea.pls.script.lexer

import com.intellij.lexer.LayeredLexer
import icu.windea.pls.model.ParadoxGameType

class ParadoxScriptLayeredLexer(
    val gameType: ParadoxGameType? = null,
) : LayeredLexer(ParadoxScriptLexer(gameType)) {
    init {
        ParadoxScriptLexerFactory.registerInlineMathLexer(this, gameType)
    }
}
