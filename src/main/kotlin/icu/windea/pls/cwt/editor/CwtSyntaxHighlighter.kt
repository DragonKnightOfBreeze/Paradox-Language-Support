package icu.windea.pls.cwt.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.editor.CwtHighlighterColorSets as ColorSets

class CwtSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> ColorSets.BRACES
            EQUAL_SIGN, NOT_EQUAL_SIGN, DOUBLE_EQUAL_SIGN -> ColorSets.OPERATOR
            DOC_COMMENT_TOKEN -> ColorSets.DOC_COMMENT
            OPTION_COMMENT_START -> ColorSets.OPTION_COMMENT
            COMMENT -> ColorSets.COMMENT
            PROPERTY_KEY_TOKEN -> ColorSets.PROPERTY_KEY
            OPTION_KEY_TOKEN -> ColorSets.OPTION_KEY
            BOOLEAN_TOKEN -> ColorSets.KEYWORD
            INT_TOKEN, FLOAT_TOKEN -> ColorSets.NUMBER
            STRING_TOKEN -> ColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> ColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return CwtLexerFactory.createHighlightingLexer(project)
    }
}
