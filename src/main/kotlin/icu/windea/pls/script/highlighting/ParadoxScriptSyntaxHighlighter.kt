package icu.windea.pls.script.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 提供基础的代码高亮。
 */
class ParadoxScriptSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> ParadoxScriptHighlighterColorSets.BRACES
            EQUAL_SIGN, NOT_EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN -> ParadoxScriptHighlighterColorSets.OPERATOR
            SAFE_ASSIGN_SIGN, SAFE_CALL_ASSIGN_SIGN -> ParadoxScriptHighlighterColorSets.OPERATOR
            NOT_SIGN -> ParadoxScriptHighlighterColorSets.OPERATOR
            PIPE, PARAMETER_START, PARAMETER_END -> ParadoxScriptHighlighterColorSets.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> ParadoxScriptHighlighterColorSets.CONDITIONAL_BLOCK_BRACKETS
            NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET -> ParadoxScriptHighlighterColorSets.CONDITIONAL_EXPRESSION_BRACKETS
            INLINE_MATH_START, INLINE_MATH_END -> ParadoxScriptHighlighterColorSets.INLINE_MATH_BRACKETS
            PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN -> ParadoxScriptHighlighterColorSets.INLINE_MATH_OPERATOR
            LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN -> ParadoxScriptHighlighterColorSets.INLINE_MATH_OPERATOR
            COMMENT -> ParadoxScriptHighlighterColorSets.COMMENT
            AT -> ParadoxScriptHighlighterColorSets.AT_SIGN
            SCRIPTED_VARIABLE_NAME_TOKEN -> ParadoxScriptHighlighterColorSets.SCRIPTED_VARIABLE_NAME
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> ParadoxScriptHighlighterColorSets.SCRIPTED_VARIABLE_REFERENCE
            PARAMETER_TOKEN -> ParadoxScriptHighlighterColorSets.PARAMETER
            CONDITION_PARAMETER_TOKEN -> ParadoxScriptHighlighterColorSets.CONDITION_PARAMETER
            ARGUMENT_TOKEN -> ParadoxScriptHighlighterColorSets.ARGUMENT
            PROPERTY_KEY_TOKEN -> ParadoxScriptHighlighterColorSets.PROPERTY_KEY
            BOOLEAN_TOKEN -> ParadoxScriptHighlighterColorSets.KEYWORD
            COLOR_TOKEN -> ParadoxScriptHighlighterColorSets.COLOR
            INT_TOKEN, FLOAT_TOKEN, INT_NUMBER_TOKEN, FLOAT_NUMBER_TOKEN -> ParadoxScriptHighlighterColorSets.NUMBER
            STRING_TOKEN -> ParadoxScriptHighlighterColorSets.STRING
            VALID_STRING_ESCAPE_TOKEN -> ParadoxScriptHighlighterColorSets.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> ParadoxScriptHighlighterColorSets.INVALID_ESCAPE
            BAD_CHARACTER -> ParadoxScriptHighlighterColorSets.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxScriptLexerFactory.createHighlightingLexer(project)
    }
}
