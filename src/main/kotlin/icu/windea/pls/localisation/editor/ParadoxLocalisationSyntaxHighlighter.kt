package icu.windea.pls.localisation.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer.ParadoxLocalisationLexerFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeyArrays as KA

class ParadoxLocalisationSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            COLON -> KA.OPERATOR_KEYS
            PIPE, COMMA -> KA.MARKER_KEYS
            COLORFUL_TEXT_START, COLORFUL_TEXT_END -> KA.MARKER_KEYS
            PARAMETER_START, PARAMETER_END -> KA.MARKER_KEYS
            LEFT_BRACKET, RIGHT_BRACKET -> KA.MARKER_KEYS
            ICON_START, ICON_END -> KA.MARKER_KEYS
            TEXT_FORMAT_START, TEXT_FORMAT_END -> KA.MARKER_KEYS
            TEXT_ICON_START, TEXT_ICON_END -> KA.MARKER_KEYS
            COMMENT -> KA.COMMENT_KEYS
            PROPERTY_NUMBER -> KA.NUMBER_KEYS
            LOCALE_TOKEN -> KA.LOCALE_KEYS
            PROPERTY_KEY_TOKEN -> KA.PROPERTY_KEY_KEYS
            ARGUMENT_TOKEN -> KA.ARGUMENT_KEYS
            COLOR_TOKEN -> KA.COLOR_ID_KEYS
            PARAMETER_TOKEN -> KA.PARAMETER_KEYS
            AT, SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_KEYS
            ICON_TOKEN -> KA.ICON_KEYS
            COMMAND_TEXT_TOKEN -> KA.COMMAND_KEYS
            CONCEPT_NAME_TOKEN -> KA.CONCEPT_KEYS
            TEXT_ICON_TOKEN -> KA.TEXT_ICON_KEYS
            TEXT_FORMAT_TOKEN -> KA.TEXT_FORMAT_KEYS
            STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> KA.STRING_KEYS
            VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
            BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
            else -> KA.EMPTY_KEYS
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxLocalisationLexerFactory.createHighlightingLexer(project, selectGameType(virtualFile))
    }
}

