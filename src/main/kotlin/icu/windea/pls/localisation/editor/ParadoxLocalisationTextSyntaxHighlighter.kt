package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeyArrays as KA

class ParadoxLocalisationTextSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        COLON -> KA.OPERATOR_KEYS
        PIPE, COMMA -> KA.MARKER_KEYS
        COLORFUL_TEXT_START, COLORFUL_TEXT_END -> KA.MARKER_KEYS
        PARAMETER_START, PARAMETER_END -> KA.MARKER_KEYS
        LEFT_BRACKET, RIGHT_BRACKET -> KA.MARKER_KEYS
        ICON_START, ICON_END -> KA.MARKER_KEYS
        TEXT_FORMAT_START, TEXT_FORMAT_END -> KA.MARKER_KEYS
        TEXT_ICON_START, TEXT_ICON_END -> KA.MARKER_KEYS
        COMMENT -> KA.COMMENT_KEYS
        ARGUMENT_TOKEN -> KA.ARGUMENT_KEYS
        COLOR_TOKEN -> KA.COLOR_ID_KEYS
        PARAMETER_TOKEN -> KA.PARAMETER_KEYS
        AT, SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_KEYS
        COMMAND_TEXT_TOKEN -> KA.COMMAND_KEYS
        ICON_TOKEN -> KA.ICON_KEYS
        CONCEPT_NAME_TOKEN -> KA.CONCEPT_KEYS
        TEXT_FORMAT_TOKEN -> KA.TEXT_FORMAT_KEYS
        TEXT_ICON_TOKEN -> KA.TEXT_ICON_KEYS
        STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> KA.STRING_KEYS
        VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
        else -> KA.EMPTY_KEYS
    }

    override fun getHighlightingLexer() = ParadoxLocalisationLexerFactory.createTextLexer(project, selectGameType(virtualFile))
}
