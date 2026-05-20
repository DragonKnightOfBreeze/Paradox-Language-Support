package icu.windea.pls.script.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColorSets as ColorSets

class ParadoxScriptSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> ColorSets.BRACES
            EQUAL_SIGN, SAFE_EQUAL_SIGN, NOT_EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN -> ColorSets.OPERATOR
            PIPE, PARAMETER_START, PARAMETER_END -> ColorSets.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> ColorSets.PARAMETER_CONDITION_BRACKETS
            NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET -> ColorSets.PARAMETER_CONDITION_EXPRESSION_BRACKETS
            INLINE_MATH_START, INLINE_MATH_END -> ColorSets.INLINE_MATH_BRACKETS
            PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN -> ColorSets.INLINE_MATH_OPERATOR
            LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN -> ColorSets.INLINE_MATH_OPERATOR
            COMMENT -> ColorSets.COMMENT
            AT -> ColorSets.AT_SIGN
            SCRIPTED_VARIABLE_NAME_TOKEN -> ColorSets.SCRIPTED_VARIABLE_NAME
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> ColorSets.SCRIPTED_VARIABLE_REFERENCE
            PARAMETER_TOKEN -> ColorSets.PARAMETER
            CONDITION_PARAMETER_TOKEN -> ColorSets.CONDITION_PARAMETER
            ARGUMENT_TOKEN -> ColorSets.ARGUMENT
            PROPERTY_KEY_TOKEN -> ColorSets.PROPERTY_KEY
            BOOLEAN_TOKEN -> ColorSets.KEYWORD
            COLOR_TOKEN -> ColorSets.COLOR
            INT_TOKEN, FLOAT_TOKEN, INT_NUMBER_TOKEN, FLOAT_NUMBER_TOKEN -> ColorSets.NUMBER
            STRING_TOKEN -> ColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> ColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxScriptLexerFactory.createHighlightingLexer(project)
    }
}
