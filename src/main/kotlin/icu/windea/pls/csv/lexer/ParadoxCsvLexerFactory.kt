package icu.windea.pls.csv.lexer

import com.intellij.lexer.*
import com.intellij.openapi.project.*
import com.intellij.psi.tree.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.model.*

object ParadoxCsvLexerFactory {
    @JvmStatic
    fun createLexer(project: Project? = null): ParadoxCsvLexer {
        return ParadoxCsvLexer()
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null, gameType: ParadoxGameType? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        lexer.registerSelfStoppingLayer(createStringLiteralLexer(COLUMN_TOKEN), arrayOf(COLUMN_TOKEN), IElementType.EMPTY_ARRAY)
        return lexer
    }

    @JvmStatic
    fun createStringLiteralLexer(elementType: IElementType): StringLiteralLexer {
        return ParadoxCsvStringLiteralLexer(elementType)
    }
}
