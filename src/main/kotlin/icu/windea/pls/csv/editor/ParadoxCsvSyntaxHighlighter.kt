package icu.windea.pls.csv.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.lexer.ParadoxCsvLexerFactory
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.csv.editor.ParadoxCsvHighlighterColorSets as ColorSets

class ParadoxCsvSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            SEPARATOR -> ColorSets.SEPARATOR
            COMMENT -> ColorSets.COMMENT
            COLUMN_TOKEN -> ColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> ColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxCsvLexerFactory.createHighlightingLexer(project)
    }
}
