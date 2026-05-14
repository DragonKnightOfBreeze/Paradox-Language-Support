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
import icu.windea.pls.localisation.editor.ParadoxLocalisationHighlighterColorSets as ColorSets

class ParadoxLocalisationSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            COLON -> ColorSets.OPERATOR
            PIPE, COMMA -> ColorSets.MARKER
            COLORFUL_TEXT_START, COLORFUL_TEXT_END -> ColorSets.MARKER
            PARAMETER_START, PARAMETER_END -> ColorSets.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> ColorSets.MARKER
            ICON_START, ICON_END -> ColorSets.MARKER
            TEXT_FORMAT_START, TEXT_FORMAT_END -> ColorSets.MARKER
            TEXT_ICON_START, TEXT_ICON_END -> ColorSets.MARKER
            COMMENT -> ColorSets.COMMENT
            PROPERTY_NUMBER -> ColorSets.NUMBER
            LOCALE_TOKEN -> ColorSets.LOCALE
            PROPERTY_KEY_TOKEN -> ColorSets.PROPERTY_KEY
            ARGUMENT_TOKEN -> ColorSets.ARGUMENT
            COLOR_TOKEN -> ColorSets.COLOR
            PARAMETER_TOKEN -> ColorSets.PARAMETER
            AT -> ColorSets.AT_SIGN
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> ColorSets.SCRIPTED_VARIABLE_REFERENCE
            ICON_TOKEN -> ColorSets.ICON
            COMMAND_TEXT_TOKEN -> ColorSets.COMMAND
            CONCEPT_NAME_TOKEN -> ColorSets.CONCEPT
            TEXT_ICON_TOKEN -> ColorSets.TEXT_ICON
            TEXT_FORMAT_TOKEN -> ColorSets.TEXT_FORMAT
            TEXT_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> ColorSets.TEXT
            VALID_STRING_ESCAPE_TOKEN -> ColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxLocalisationLexerFactory.createHighlightingLexer(project, selectGameType(virtualFile))
    }
}

