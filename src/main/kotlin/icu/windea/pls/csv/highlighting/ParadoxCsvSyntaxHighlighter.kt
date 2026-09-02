package icu.windea.pls.csv.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.lexer.ParadoxCsvLexerFactory
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

/**
 * 提供基础的代码高亮。
 */
class ParadoxCsvSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            SEPARATOR -> ParadoxCsvHighlighterColorSets.SEPARATOR
            COMMENT -> ParadoxCsvHighlighterColorSets.COMMENT
            COLUMN_TOKEN -> ParadoxCsvHighlighterColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> ParadoxCsvHighlighterColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ParadoxCsvHighlighterColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ParadoxCsvHighlighterColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxCsvLexerFactory.createHighlightingLexer(project)
    }
}
