package icu.windea.pls.cwt.lexer

import com.intellij.lexer.LayeredLexer

class CwtHighlightingLexer : LayeredLexer(CwtLexer()) {
    init {
        CwtLexerFactory.registerLiteralLexer(this)
    }
}
