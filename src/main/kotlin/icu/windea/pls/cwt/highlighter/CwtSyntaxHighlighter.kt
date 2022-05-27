package icu.windea.pls.cwt.highlighter

import com.intellij.lexer.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtSyntaxHighlighter : SyntaxHighlighterBase() {
	companion object {
		private val BRACES_KEYS = arrayOf(CwtAttributesKeys.BRACES_KEY)
		private val OPERATOR_KEYS = arrayOf(CwtAttributesKeys.OPERATOR_KEY)
		private val COMMENT_KEYS = arrayOf(CwtAttributesKeys.COMMENT_KEY)
		private val OPTION_COMMENT_KEYS = arrayOf(CwtAttributesKeys.OPTION_COMMENT_KEY)
		private val DOCUMENTATION_COMMENT_KEYS = arrayOf(CwtAttributesKeys.DOCUMENTATION_COMMENT_KEY)
		private val KEYWORD_KEYS = arrayOf(CwtAttributesKeys.KEYWORD_KEY)
		private val PROPERTY_KEY_KEYS = arrayOf(CwtAttributesKeys.PROPERTY_KEY_KEY)
		private val OPTION_KEY_KEYS = arrayOf(CwtAttributesKeys.OPTION_KEY_KEY)
		private val NUMBER_KEYS = arrayOf(CwtAttributesKeys.NUMBER_KEY)
		private val STRING_KEYS = arrayOf(CwtAttributesKeys.STRING_KEY)
		private val VALID_ESCAPE_KEYS = arrayOf(CwtAttributesKeys.VALID_ESCAPE_KEY)
		private val INVALID_ESCAPE_KEYS = arrayOf(CwtAttributesKeys.INVALID_ESCAPE_KEY)
		private val BAD_CHARACTER_KEYS = arrayOf(CwtAttributesKeys.BAD_CHARACTER_KEY)
		private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
	}
	
	override fun getTokenHighlights(tokenType: IElementType?) = when(tokenType) {
		LEFT_BRACE, RIGHT_BRACE -> BRACES_KEYS
		EQUAL_SIGN, NOT_EQUAL_SIGN -> OPERATOR_KEYS
		COMMENT -> COMMENT_KEYS
		OPTION_START -> OPTION_COMMENT_KEYS
		DOCUMENTATION_START, DOCUMENTATION_TOKEN -> DOCUMENTATION_COMMENT_KEYS
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
	
	override fun getHighlightingLexer(): Lexer = CwtLexerAdapter()
}