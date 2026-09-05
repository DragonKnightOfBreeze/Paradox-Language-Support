package icu.windea.pls.csv.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

class ParadoxCsvHighlightingLexer : LayeredLexer(ParadoxCsvLexer()) {
    init {
        ParadoxCsvLexerFactory.registerLiteralLexer(this)
    }
}
