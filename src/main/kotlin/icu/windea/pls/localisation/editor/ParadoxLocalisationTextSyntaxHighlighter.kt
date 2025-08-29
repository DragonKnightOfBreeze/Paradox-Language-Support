package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer.ParadoxLocalisationLexerFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ARGUMENT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.AT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLON
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLOR_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMA
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMAND_TEXT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMENT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_NAME_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_BRACKET
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PIPE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_BRACKET
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_SINGLE_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.STRING_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ICON_TOKEN
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
