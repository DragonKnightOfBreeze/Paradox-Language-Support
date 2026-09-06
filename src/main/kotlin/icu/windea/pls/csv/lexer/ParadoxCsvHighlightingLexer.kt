package icu.windea.pls.csv.lexer

import com.intellij.lexer.LayeredLexer

class ParadoxCsvHighlightingLexer : LayeredLexer(ParadoxCsvLexer()) {
    init {
        ParadoxCsvLexerFactory.registerLiteralLexer(this)
    }
}
