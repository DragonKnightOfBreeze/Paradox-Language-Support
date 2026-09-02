package icu.windea.pls.localisation.highlighting

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

/**
 * 提供基础的代码高亮。
 */
class ParadoxLocalisationSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            COLON -> ParadoxLocalisationHighlighterColorSets.OPERATOR
            PIPE, COMMA -> ParadoxLocalisationHighlighterColorSets.MARKER
            COLORFUL_TEXT_START, COLORFUL_TEXT_END -> ParadoxLocalisationHighlighterColorSets.MARKER
            PARAMETER_START, PARAMETER_END -> ParadoxLocalisationHighlighterColorSets.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> ParadoxLocalisationHighlighterColorSets.MARKER
            ICON_START, ICON_END -> ParadoxLocalisationHighlighterColorSets.MARKER
            TEXT_FORMAT_START, TEXT_FORMAT_END -> ParadoxLocalisationHighlighterColorSets.MARKER
            TEXT_ICON_START, TEXT_ICON_END -> ParadoxLocalisationHighlighterColorSets.MARKER
            COMMENT -> ParadoxLocalisationHighlighterColorSets.COMMENT
            PROPERTY_NUMBER_TOKEN -> ParadoxLocalisationHighlighterColorSets.NUMBER
            LOCALE_TOKEN -> ParadoxLocalisationHighlighterColorSets.LOCALE
            PROPERTY_KEY_TOKEN -> ParadoxLocalisationHighlighterColorSets.PROPERTY_KEY
            ARGUMENT_TOKEN -> ParadoxLocalisationHighlighterColorSets.ARGUMENT
            COLOR_TOKEN -> ParadoxLocalisationHighlighterColorSets.COLOR
            PARAMETER_TOKEN -> ParadoxLocalisationHighlighterColorSets.PARAMETER
            AT -> ParadoxLocalisationHighlighterColorSets.AT_SIGN
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> ParadoxLocalisationHighlighterColorSets.SCRIPTED_VARIABLE_REFERENCE
            ICON_TOKEN -> ParadoxLocalisationHighlighterColorSets.ICON
            COMMAND_TEXT_TOKEN -> ParadoxLocalisationHighlighterColorSets.COMMAND
            CONCEPT_NAME_TOKEN -> ParadoxLocalisationHighlighterColorSets.CONCEPT
            TEXT_ICON_TOKEN -> ParadoxLocalisationHighlighterColorSets.TEXT_ICON
            TEXT_FORMAT_TOKEN -> ParadoxLocalisationHighlighterColorSets.TEXT_FORMAT
            TEXT_TOKEN -> ParadoxLocalisationHighlighterColorSets.TEXT
            LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> ParadoxLocalisationHighlighterColorSets.TEXT
            VALID_STRING_ESCAPE_TOKEN -> ParadoxLocalisationHighlighterColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ParadoxLocalisationHighlighterColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ParadoxLocalisationHighlighterColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxLocalisationLexerFactory.createHighlightingLexer(project, selectGameType(virtualFile))
    }
}
