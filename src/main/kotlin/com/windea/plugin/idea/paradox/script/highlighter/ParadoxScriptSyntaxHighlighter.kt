@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.script.highlighter

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptSyntaxHighlighter : SyntaxHighlighterBase() {
	companion object {
		private val SEPARATOR_KEYS = arrayOf(ParadoxScriptAttributesKeys.SEPARATOR_KEY)
		private val BRACE_KEYS = arrayOf(ParadoxScriptAttributesKeys.BRACES_KEY)
		private val VARIABLE_KEYS = arrayOf(ParadoxScriptAttributesKeys.VARIABLE_KEY)
		private val PROPERTY_KEY_KEYS = arrayOf(ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY)
		private val KEYWORD_KEYS = arrayOf(ParadoxScriptAttributesKeys.KEYWORD_KEY)
		private val COLOR_KEYS = arrayOf(ParadoxScriptAttributesKeys.COLOR_KEY)
		private val NUMBER_KEYS = arrayOf(ParadoxScriptAttributesKeys.NUMBER_KEY)
		private val STRING_KEYS = arrayOf(ParadoxScriptAttributesKeys.STRING_KEY)
		private val CODE_KEYS = arrayOf(ParadoxScriptAttributesKeys.CODE_KEY)
		private val COMMENT_KEYS = arrayOf(ParadoxScriptAttributesKeys.COMMENT_KEY)
		private val VALID_ESCAPE_KEYS = arrayOf(ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY)
		private val INVALID_ESCAPE_KEYS = arrayOf(ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY)
		private val BAD_CHARACTER_KEYS = arrayOf(ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY)
		private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
	}
	
	override fun getTokenHighlights(tokenType: IElementType?) = when(tokenType) {
		EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN -> SEPARATOR_KEYS
		LEFT_BRACE, RIGHT_BRACE, CODE_START, CODE_END -> BRACE_KEYS
		VARIABLE_NAME_ID -> VARIABLE_KEYS
		PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID -> PROPERTY_KEY_KEYS
		VARIABLE_REFERENCE_ID -> VARIABLE_KEYS
		BOOLEAN_TOKEN -> KEYWORD_KEYS
		COLOR_TOKEN -> COLOR_KEYS
		NUMBER_TOKEN -> NUMBER_KEYS
		STRING_TOKEN, QUOTED_STRING_TOKEN -> STRING_KEYS
		CODE_TEXT_TOKEN -> CODE_KEYS
		COMMENT -> COMMENT_KEYS
		END_OF_LINE_COMMENT -> COMMENT_KEYS
		VALID_STRING_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
		INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
		BAD_CHARACTER -> BAD_CHARACTER_KEYS
		else -> EMPTY_KEYS
	}
	
	override fun getHighlightingLexer() = ParadoxScriptLexerAdapter()
}
