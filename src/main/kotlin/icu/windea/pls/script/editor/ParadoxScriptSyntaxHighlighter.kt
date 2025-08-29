package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.AT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BOOLEAN_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COLOR_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COMMENT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.CONDITION_PARAMETER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.DIV_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.FLOAT_NUMBER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.FLOAT_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.GE_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.GT_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_END
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_START
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INT_NUMBER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INT_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LABS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LEFT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LEFT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LE_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LP_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LT_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.MINUS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.MOD_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NESTED_LEFT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NESTED_RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NOT_EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_END
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_START
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PIPE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PLUS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RABS_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RP_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.TIMES_SIGN
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeyArrays as KA

class ParadoxScriptSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        LEFT_BRACE, RIGHT_BRACE -> KA.BRACES_KEYS
        EQUAL_SIGN, NOT_EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN, SAFE_EQUAL_SIGN -> KA.OPERATOR_KEYS
        PIPE, PARAMETER_START, PARAMETER_END -> KA.MARKER_KEYS
        LEFT_BRACKET, RIGHT_BRACKET -> KA.PARAMETER_CONDITION_BRACKETS_KEYS
        NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET -> KA.PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS
        INLINE_MATH_START, INLINE_MATH_END -> KA.INLINE_MATH_BRACES_KEYS
        PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN -> KA.INLINE_MATH_OPERATOR_KEYS
        LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN -> KA.INLINE_MATH_OPERATOR_KEYS
        COMMENT -> KA.COMMENT_KEYS
        AT -> KA.SCRIPTED_VARIABLE_KEYS
        SCRIPTED_VARIABLE_NAME_TOKEN -> KA.SCRIPTED_VARIABLE_KEYS
        SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_KEYS
        INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_KEYS
        CONDITION_PARAMETER_TOKEN -> KA.CONDITION_PARAMETER_KEYS
        PARAMETER_TOKEN -> KA.PARAMETER_KEYS
        PROPERTY_KEY_TOKEN -> KA.PROPERTY_KEY_KEYS
        BOOLEAN_TOKEN -> KA.KEYWORD_KEYS
        COLOR_TOKEN -> KA.COLOR_KEYS
        INT_TOKEN, FLOAT_TOKEN, INT_NUMBER_TOKEN, FLOAT_NUMBER_TOKEN -> KA.NUMBER_KEYS
        STRING_TOKEN -> KA.STRING_KEYS
        VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
        else -> KA.EMPTY_KEYS
    }

    override fun getHighlightingLexer() = ParadoxScriptLexerFactory.createHighlightingLexer(project)
}
