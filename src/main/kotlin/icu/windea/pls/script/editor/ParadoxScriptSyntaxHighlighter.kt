package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.lexer.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
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
