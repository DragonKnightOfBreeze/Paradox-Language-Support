@file:Suppress("HasPlatformType")

package com.windea.plugin.idea.paradox.localisation.highlighter

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationSyntaxHighlighter : SyntaxHighlighterBase() {
	companion object {
		private val SEPARATOR_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.SEPARATOR_KEY)
		private val NUMBER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.NUMBER_KEY)
		private val LOCALE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.LOCALE_KEY)
		private val PROPERTY_KEY_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
		private val STRING_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.STRING_KEY)
		private val COMMENT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMENT_KEY)
		private val MARKER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.MARKER_KEY)
		private val PARAMETER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PARAMETER_KEY)
		private val PROPERTY_REFERENCE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY)
		private val COMMAND_KEY_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMAND_KEY_KEY)
		private val ICON_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.ICON_KEY)
		private val SERIAL_NUMBER_ID_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.SERIAL_NUMBER_KEY)
		private val COLOR_ID_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COLOR_KEY)
		private val VALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY)
		private val INVALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY)
		private val BAD_CHARACTER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY)
		private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
	}

	override fun getTokenHighlights(tokenType: IElementType?) = when(tokenType) {
		COLON,COMMAND_KEY_SEPARATOR -> SEPARATOR_KEYS
		LOCALE_ID -> LOCALE_KEYS
		PROPERTY_KEY_ID -> PROPERTY_KEY_KEYS
		PROPERTY_REFERENCE_ID -> PROPERTY_REFERENCE_KEYS
		STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE -> STRING_KEYS
		PROPERTY_REFERENCE_START, PARAMETER_SEPARATOR, PROPERTY_REFERENCE_END,
		COMMAND_START, COMMAND_END, ICON_START, ICON_END,
		SERIAL_NUMBER_START, SERIAL_NUMBER_END, COLORFUL_TEXT_START, COLORFUL_TEXT_END -> MARKER_KEYS
		PROPERTY_REFERENCE_PARAMETER, ICON_PARAMETER -> PARAMETER_KEYS
		NUMBER -> NUMBER_KEYS
		COMMAND_KEY -> COMMAND_KEY_KEYS
		ICON_ID -> ICON_KEYS
		SERIAL_NUMBER_ID -> SERIAL_NUMBER_ID_KEYS
		COLOR_CODE -> COLOR_ID_KEYS
		COMMENT, END_OF_LINE_COMMENT, ROOT_COMMENT -> COMMENT_KEYS
		VALID_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
		INVALID_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
		BAD_CHARACTER -> BAD_CHARACTER_KEYS
		else -> EMPTY_KEYS
	}

	override fun getHighlightingLexer() = ParadoxLocalisationLexerAdapter()
}

