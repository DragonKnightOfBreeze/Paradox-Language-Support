package icu.windea.pls.localisation.highlighter

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    companion object {
        private val OPERATOR_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.OPERATOR_KEY)
        private val MARKER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.MARKER_KEY)
        private val NUMBER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.NUMBER_KEY)
        private val LOCALE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.LOCALE_KEY)
        private val PROPERTY_KEY_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
        private val COMMENT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMENT_KEY)
        private val PROPERTY_REFERENCE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY)
        private val PROPERTY_REFERENCE_PARAMETER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_PARAMETER_KEY)
        private val SCRIPTED_VARIABLE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.SCRIPTED_VARIABLE_KEY)
        private val ICON_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.ICON_KEY)
        private val COMMAND_SCOPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY)
        private val COMMAND_FIELD_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY)
        private val CONCEPT_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.CONCEPT_KEY)
        private val COLOR_ID_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.COLOR_KEY)
        private val STRING_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.STRING_KEY)
        private val VALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY)
        private val INVALID_ESCAPE_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY)
        private val BAD_CHARACTER_KEYS = arrayOf(ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY)
        private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
    }
    
    override fun getTokenHighlights(tokenType: IElementType?) = when(tokenType) {
        COLON, DOT -> OPERATOR_KEYS
        PROPERTY_REFERENCE_START, PROPERTY_REFERENCE_END, ICON_START, ICON_END -> MARKER_KEYS
        COMMAND_START, COMMAND_END, COLORFUL_TEXT_START, COLORFUL_TEXT_END -> MARKER_KEYS
        PIPE, COMMA -> MARKER_KEYS
        COMMENT -> COMMENT_KEYS
        AT, SCRIPTED_VARIABLE_REFERENCE_TOKEN -> SCRIPTED_VARIABLE_KEYS
        LOCALE_TOKEN -> LOCALE_KEYS
        PROPERTY_KEY_TOKEN -> PROPERTY_KEY_KEYS
        PROPERTY_REFERENCE_TOKEN -> PROPERTY_REFERENCE_KEYS
        PROPERTY_REFERENCE_PARAMETER_TOKEN -> PROPERTY_REFERENCE_PARAMETER_KEYS
        STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> STRING_KEYS
        PROPERTY_NUMBER, ICON_FRAME -> NUMBER_KEYS
        ICON_TOKEN -> ICON_KEYS
        COMMAND_SCOPE_TOKEN -> COMMAND_SCOPE_KEYS
        COMMAND_FIELD_TOKEN -> COMMAND_FIELD_KEYS
        CONCEPT_NAME_TOKEN -> CONCEPT_KEYS
        COLOR_TOKEN -> COLOR_ID_KEYS
        VALID_STRING_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> EMPTY_KEYS
    }
    
    override fun getHighlightingLexer() = ParadoxLocalisationLexer()
}

