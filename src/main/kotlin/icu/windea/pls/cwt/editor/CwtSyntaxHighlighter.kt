package icu.windea.pls.cwt.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.lexer.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.editor.CwtAttributesKeyArrays as KA

class CwtSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        LEFT_BRACE, RIGHT_BRACE -> KA.BRACES_KEYS
        EQUAL_SIGN, NOT_EQUAL_SIGN -> KA.OPERATOR_KEYS
        DOC_COMMENT_TOKEN -> KA.DOC_COMMENT_KEYS
        OPTION_COMMENT_TOKEN, OPTION_COMMENT_START -> KA.OPTION_COMMENT_KEYS
        COMMENT -> KA.COMMENT_KEYS
        PROPERTY_KEY_TOKEN -> KA.PROPERTY_KEY_KEYS
        OPTION_KEY_TOKEN -> KA.OPTION_KEY_KEYS
        BOOLEAN_TOKEN -> KA.KEYWORD_KEYS
        INT_TOKEN, FLOAT_TOKEN -> KA.NUMBER_KEYS
        STRING_TOKEN -> KA.STRING_KEYS
        VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
        else -> KA.EMPTY_KEYS
    }

    override fun getHighlightingLexer() = CwtLexerFactory.createHighlightingLexer(project)
}
