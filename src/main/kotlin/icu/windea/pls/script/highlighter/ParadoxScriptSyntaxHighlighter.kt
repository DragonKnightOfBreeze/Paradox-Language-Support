package icu.windea.pls.script.highlighter

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    companion object {
        private val BRACES_KEYS = arrayOf(ParadoxScriptAttributesKeys.BRACES_KEY)
        private val OPERATOR_KEYS = arrayOf(ParadoxScriptAttributesKeys.OPERATOR_KEY)
        private val MARKER_KEYS = arrayOf(ParadoxScriptAttributesKeys.MARKER_KEY)
        private val PARAMETER_CONDITION_BRACKETS_KEYS = arrayOf(ParadoxScriptAttributesKeys.PARAMETER_CONDITION_BRACKETS_KEYS)
        private val PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS = arrayOf(ParadoxScriptAttributesKeys.PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS)
        private val INLINE_MATH_BRACES_KEYS = arrayOf(ParadoxScriptAttributesKeys.INLINE_MATH_BRACES_KEY)
        private val INLINE_MATH_OPERATOR_KEYS = arrayOf(ParadoxScriptAttributesKeys.INLINE_MATH_OPERATOR_KEY)
        private val KEYWORD_KEYS = arrayOf(ParadoxScriptAttributesKeys.KEYWORD_KEY)
        private val COMMENT_KEYS = arrayOf(ParadoxScriptAttributesKeys.COMMENT_KEY)
        private val SCRIPTED_VARIABLE_KEYS = arrayOf(ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_KEY)
        private val PARAMETER_KEYS = arrayOf(ParadoxScriptAttributesKeys.PARAMETER_KEY)
        private val CONDITION_PARAMETER_KEYS = arrayOf(ParadoxScriptAttributesKeys.CONDITION_PARAMETER_KEY)
        private val PROPERTY_KEY_KEYS = arrayOf(ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY)
        private val COLOR_KEYS = arrayOf(ParadoxScriptAttributesKeys.COLOR_KEY)
        private val NUMBER_KEYS = arrayOf(ParadoxScriptAttributesKeys.NUMBER_KEY)
        private val STRING_KEYS = arrayOf(ParadoxScriptAttributesKeys.STRING_KEY)
        private val VALID_ESCAPE_KEYS = arrayOf(ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY)
        private val INVALID_ESCAPE_KEYS = arrayOf(ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY)
        private val BAD_CHARACTER_KEYS = arrayOf(ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY)
        private val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
    }
    
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when(tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> BRACES_KEYS
            EQUAL_SIGN, NOT_EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN -> OPERATOR_KEYS
            PIPE, PARAMETER_START, PARAMETER_END -> MARKER_KEYS
            LEFT_BRACKET, RIGHT_BRACKET -> PARAMETER_CONDITION_BRACKETS_KEYS
            NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET -> PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS
            INLINE_MATH_START, INLINE_MATH_END -> INLINE_MATH_BRACES_KEYS
            PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN -> INLINE_MATH_OPERATOR_KEYS
            LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN -> INLINE_MATH_OPERATOR_KEYS
            COMMENT -> COMMENT_KEYS
            AT -> SCRIPTED_VARIABLE_KEYS
            SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_NAME_SNIPPET -> SCRIPTED_VARIABLE_KEYS
            SCRIPTED_VARIABLE_REFERENCE_TOKEN, SCRIPTED_VARIABLE_REFERENCE_SNIPPET -> SCRIPTED_VARIABLE_KEYS
            INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN -> SCRIPTED_VARIABLE_KEYS
            CONDITION_PARAMETER_TOKEN -> CONDITION_PARAMETER_KEYS
            PARAMETER_TOKEN -> PARAMETER_KEYS
            QUOTED_PROPERTY_KEY_TOKEN, PROPERTY_KEY_TOKEN, PROPERTY_KEY_SNIPPET -> PROPERTY_KEY_KEYS
            BOOLEAN_TOKEN -> KEYWORD_KEYS
            COLOR_TOKEN -> COLOR_KEYS
            INT_TOKEN, FLOAT_TOKEN, INT_NUMBER_TOKEN, FLOAT_NUMBER_TOKEN -> NUMBER_KEYS
            QUOTED_STRING_TOKEN, STRING_TOKEN, STRING_SNIPPET -> STRING_KEYS
            VALID_STRING_ESCAPE_TOKEN -> VALID_ESCAPE_KEYS
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> INVALID_ESCAPE_KEYS
            BAD_CHARACTER -> BAD_CHARACTER_KEYS
            else -> EMPTY_KEYS
        }
    }
    
    override fun getHighlightingLexer(): ParadoxScriptLexerAdapter {
        return ParadoxScriptLexerAdapter()
    }
}
