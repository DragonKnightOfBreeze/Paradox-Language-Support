package icu.windea.pls.csv.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvLexerFactory {
    private val columnTokens = arrayOf(COLUMN_TOKEN)
    private val emptyTokens = IElementType.EMPTY_ARRAY

    @JvmStatic
    fun createLexer(project: Project? = null): ParadoxCsvLexer {
        return ParadoxCsvLexer()
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null): ParadoxCsvHighlightingLexer {
        return ParadoxCsvHighlightingLexer()
    }

    @JvmStatic
    fun registerLiteralLexer(lexer: LayeredLexer) {
        lexer.registerSelfStoppingLayer(ParadoxCsvStringLiteralLexer(COLUMN_TOKEN), columnTokens, emptyTokens)
    }
}
