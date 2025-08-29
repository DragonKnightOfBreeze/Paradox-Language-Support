package icu.windea.pls.csv.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COLUMN_TOKEN
import icu.windea.pls.model.ParadoxGameType

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
