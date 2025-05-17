package icu.windea.pls.localisation.editor

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    companion object {
        private val OPERATOR_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.OPERATOR_KEY)
        private val MARKER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.MARKER_KEY)
        private val COMMENT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMENT_KEY)
        private val NUMBER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.NUMBER_KEY)
        private val LOCALE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.LOCALE_KEY)
        private val PROPERTY_KEY_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
        private val COLOR_ID_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COLOR_KEY)
        private val PROPERTY_REFERENCE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY)
        private val PROPERTY_REFERENCE_ARGUMENT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_ARGUMENT_KEY)
        private val SCRIPTED_VARIABLE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.SCRIPTED_VARIABLE_KEY)
        private val COMMAND_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMAND_KEY)
        private val COMMAND_ARGUMENT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMAND_ARGUMENT_KEY)
        private val ICON_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.ICON_KEY)
        private val CONCEPT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.CONCEPT_KEY)
        private val TEXT_FORMAT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.TEXT_FORMAT_KEY)
        private val TEXT_ICON_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.TEXT_ICON_KEY)
        private val STRING_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.STRING_KEY)
        private val VALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY)
        private val INVALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY)
        private val BAD_CHARACTER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY)
        private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
    }

    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        COLON -> OPERATOR_KEYS
        PIPE, COMMA -> MARKER_KEYS
        COLORFUL_TEXT_START, COLORFUL_TEXT_END -> MARKER_KEYS
        PROPERTY_REFERENCE_START, PROPERTY_REFERENCE_END -> MARKER_KEYS
        COMMAND_START, COMMAND_END -> MARKER_KEYS
        ICON_START, ICON_END -> MARKER_KEYS
        TEXT_FORMAT_START, TEXT_FORMAT_END -> MARKER_KEYS
        TEXT_ICON_START, TEXT_ICON_END -> MARKER_KEYS
        COMMENT -> COMMENT_KEYS
        PROPERTY_NUMBER -> NUMBER_KEYS
        LOCALE_TOKEN -> LOCALE_KEYS
        PROPERTY_KEY_TOKEN -> PROPERTY_KEY_KEYS
        STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> STRING_KEYS
        COLOR_TOKEN -> COLOR_ID_KEYS
        PROPERTY_REFERENCE_TOKEN -> PROPERTY_REFERENCE_KEYS
        PROPERTY_REFERENCE_ARGUMENT_TOKEN -> PROPERTY_REFERENCE_ARGUMENT_KEYS
        AT, SCRIPTED_VARIABLE_REFERENCE_TOKEN -> SCRIPTED_VARIABLE_KEYS
        COMMAND_TEXT_TOKEN -> COMMAND_KEYS
        COMMAND_ARGUMENT_TOKEN -> COMMAND_ARGUMENT_KEYS
        ICON_TOKEN -> ICON_KEYS
        ICON_ARGUMENT_TOKEN -> NUMBER_KEYS //use NUMBER_KEYS here
        CONCEPT_NAME_TOKEN -> CONCEPT_KEYS
        TEXT_FORMAT_TOKEN -> TEXT_FORMAT_KEYS
        TEXT_ICON_TOKEN -> TEXT_ICON_KEYS
        VALID_STRING_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> EMPTY_KEYS
    }

    override fun getHighlightingLexer() = ParadoxLocalisationLexerFactory.createHighlightingLexer(project, selectGameType(virtualFile))
}

