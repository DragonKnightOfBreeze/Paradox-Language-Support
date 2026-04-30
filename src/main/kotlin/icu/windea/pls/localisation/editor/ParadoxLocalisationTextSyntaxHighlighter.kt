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
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeySets as KA

class ParadoxLocalisationTextSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            COLON -> KA.OPERATOR
            PIPE, COMMA -> KA.MARKER
            COLORFUL_TEXT_START, COLORFUL_TEXT_END -> KA.MARKER
            PARAMETER_START, PARAMETER_END -> KA.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> KA.MARKER
            ICON_START, ICON_END -> KA.MARKER
            TEXT_FORMAT_START, TEXT_FORMAT_END -> KA.MARKER
            TEXT_ICON_START, TEXT_ICON_END -> KA.MARKER
            COMMENT -> KA.COMMENT
            ARGUMENT_TOKEN -> KA.ARGUMENT
            COLOR_TOKEN -> KA.COLOR
            PARAMETER_TOKEN -> KA.PARAMETER
            AT -> KA.AT_SIGN
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_REFERENCE
            ICON_TOKEN -> KA.ICON
            COMMAND_TEXT_TOKEN -> KA.COMMAND
            CONCEPT_NAME_TOKEN -> KA.CONCEPT
            TEXT_ICON_TOKEN -> KA.TEXT_ICON
            TEXT_FORMAT_TOKEN -> KA.TEXT_FORMAT
            TEXT_TOKEN, LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE -> KA.TEXT
            VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE
            BAD_CHARACTER -> KA.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxLocalisationLexerFactory.createTextLexer(project, selectGameType(virtualFile))
    }
}
