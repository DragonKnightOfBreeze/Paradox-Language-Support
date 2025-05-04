package icu.windea.pls.cwt.editor

import com.intellij.lexer.*
import com.intellij.lexer.StringLiteralLexer.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtSyntaxHighlighter : SyntaxHighlighter {
    companion object {
        private val BRACES_KEYS = arrayOf(CwtAttributesKeys.BRACES_KEY)
        private val OPERATOR_KEYS = arrayOf(CwtAttributesKeys.OPERATOR_KEY)
        private val COMMENT_KEYS = arrayOf(CwtAttributesKeys.COMMENT_KEY)
        private val OPTION_COMMENT_KEYS = arrayOf(CwtAttributesKeys.OPTION_COMMENT_KEY)
        private val DOC_COMMENT_KEYS = arrayOf(CwtAttributesKeys.DOC_COMMENT_KEY)
        private val KEYWORD_KEYS = arrayOf(CwtAttributesKeys.KEYWORD_KEY)
        private val PROPERTY_KEY_KEYS = arrayOf(CwtAttributesKeys.PROPERTY_KEY_KEY)
        private val OPTION_KEY_KEYS = arrayOf(CwtAttributesKeys.OPTION_KEY_KEY)
        private val NUMBER_KEYS = arrayOf(CwtAttributesKeys.NUMBER_KEY)
        private val STRING_KEYS = arrayOf(CwtAttributesKeys.STRING_KEY)
        private val VALID_ESCAPE_KEYS = arrayOf(CwtAttributesKeys.VALID_ESCAPE_KEY)
        private val INVALID_ESCAPE_KEYS = arrayOf(CwtAttributesKeys.INVALID_ESCAPE_KEY)
        private val BAD_CHARACTER_KEYS = arrayOf(CwtAttributesKeys.BAD_CHARACTER_KEY)
        private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY

        private const val additionalValidEscapes = "$"
    }

    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        LEFT_BRACE, RIGHT_BRACE -> BRACES_KEYS
        EQUAL_SIGN, NOT_EQUAL_SIGN -> OPERATOR_KEYS
        DOC_COMMENT_TOKEN -> DOC_COMMENT_KEYS
        OPTION_COMMENT_TOKEN, OPTION_COMMENT_START -> OPTION_COMMENT_KEYS
        COMMENT -> COMMENT_KEYS
        PROPERTY_KEY_TOKEN -> PROPERTY_KEY_KEYS
        OPTION_KEY_TOKEN -> OPTION_KEY_KEYS
        BOOLEAN_TOKEN -> KEYWORD_KEYS
        INT_TOKEN, FLOAT_TOKEN -> NUMBER_KEYS
        STRING_TOKEN -> STRING_KEYS
        VALID_STRING_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> EMPTY_KEYS
    }

    override fun getHighlightingLexer(): Lexer {
        val lexer = LayeredLexer(CwtLexer())
        val optionLexer = LayeredLexer(CwtOptionLexer())
        val lexer1 = StringLiteralLexer(NO_QUOTE_CHAR, PROPERTY_KEY_TOKEN, false, additionalValidEscapes, false, false)
        val lexer2 = StringLiteralLexer(NO_QUOTE_CHAR, STRING_TOKEN, false, additionalValidEscapes, false, false)
        lexer.registerSelfStoppingLayer(optionLexer, arrayOf(OPTION_COMMENT_TOKEN), emptyArray())
        lexer.registerSelfStoppingLayer(lexer1, arrayOf(PROPERTY_KEY_TOKEN), emptyArray())
        lexer.registerSelfStoppingLayer(lexer2, arrayOf(STRING_TOKEN), emptyArray())
        optionLexer.registerSelfStoppingLayer(lexer1, arrayOf(PROPERTY_KEY_TOKEN), emptyArray())
        optionLexer.registerSelfStoppingLayer(lexer2, arrayOf(STRING_TOKEN), emptyArray())
        return lexer
    }
}
