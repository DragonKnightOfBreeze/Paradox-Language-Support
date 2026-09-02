package icu.windea.pls.cwt.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.psi.CwtElementTypes.*

/**
 * 提供基础的代码高亮。
 */
class CwtSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> CwtHighlighterColorSets.BRACES
            EQUAL_SIGN, NOT_EQUAL_SIGN, DOUBLE_EQUAL_SIGN -> CwtHighlighterColorSets.OPERATOR
            DOC_COMMENT_TOKEN -> CwtHighlighterColorSets.DOC_COMMENT
            OPTION_COMMENT_START -> CwtHighlighterColorSets.OPTION_COMMENT
            COMMENT -> CwtHighlighterColorSets.COMMENT
            PROPERTY_KEY_TOKEN -> CwtHighlighterColorSets.PROPERTY_KEY
            OPTION_KEY_TOKEN -> CwtHighlighterColorSets.OPTION_KEY
            BOOLEAN_TOKEN -> CwtHighlighterColorSets.KEYWORD
            INT_TOKEN, FLOAT_TOKEN -> CwtHighlighterColorSets.NUMBER
            STRING_TOKEN -> CwtHighlighterColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> CwtHighlighterColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> CwtHighlighterColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> CwtHighlighterColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return CwtLexerFactory.createHighlightingLexer(project)
    }
}
