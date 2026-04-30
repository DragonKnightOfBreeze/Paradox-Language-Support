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
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeySets as KA

class ParadoxScriptSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> KA.BRACES
            EQUAL_SIGN, SAFE_EQUAL_SIGN, NOT_EQUAL_SIGN, LE_SIGN, LT_SIGN, GE_SIGN, GT_SIGN -> KA.OPERATOR
            PIPE, PARAMETER_START, PARAMETER_END -> KA.MARKER
            LEFT_BRACKET, RIGHT_BRACKET -> KA.PARAMETER_CONDITION_BRACKETS
            NESTED_LEFT_BRACKET, NESTED_RIGHT_BRACKET -> KA.PARAMETER_CONDITION_EXPRESSION_BRACKETS
            INLINE_MATH_START, INLINE_MATH_END -> KA.INLINE_MATH_BRACKETS
            PLUS_SIGN, MINUS_SIGN, TIMES_SIGN, DIV_SIGN, MOD_SIGN -> KA.INLINE_MATH_OPERATOR
            LABS_SIGN, RABS_SIGN, LP_SIGN, RP_SIGN -> KA.INLINE_MATH_OPERATOR
            COMMENT -> KA.COMMENT
            AT -> KA.AT_SIGN
            SCRIPTED_VARIABLE_NAME_TOKEN -> KA.SCRIPTED_VARIABLE_NAME
            SCRIPTED_VARIABLE_REFERENCE_TOKEN -> KA.SCRIPTED_VARIABLE_REFERENCE
            CONDITION_PARAMETER_TOKEN -> KA.CONDITION_PARAMETER
            PARAMETER_TOKEN -> KA.PARAMETER
            PROPERTY_KEY_TOKEN -> KA.PROPERTY_KEY
            BOOLEAN_TOKEN -> KA.KEYWORD
            COLOR_TOKEN -> KA.COLOR
            INT_TOKEN, FLOAT_TOKEN, INT_NUMBER_TOKEN, FLOAT_NUMBER_TOKEN -> KA.NUMBER
            STRING_TOKEN -> KA.STRING
            VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE
            BAD_CHARACTER -> KA.BAD_CHARACTER
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return ParadoxScriptLexerFactory.createHighlightingLexer(project)
    }
}
