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
import icu.windea.pls.csv.editor.ParadoxCsvAttributesKeyArrays as KA

class ParadoxCsvSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            SEPARATOR -> KA.SEPARATOR_KEYS
            COMMENT -> KA.COMMENT_KEYS
            COLUMN_TOKEN -> KA.STRING_KEYS
            VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
            BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
            else -> KA.EMPTY_KEYS
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxCsvLexerFactory.createHighlightingLexer(project)
    }
}
