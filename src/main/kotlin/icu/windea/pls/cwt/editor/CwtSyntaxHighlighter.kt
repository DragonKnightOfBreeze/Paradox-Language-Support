package icu.windea.pls.cwt.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.psi.StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.psi.CwtElementTypes.BOOLEAN_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.COMMENT
import icu.windea.pls.cwt.psi.CwtElementTypes.DOC_COMMENT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.EQUAL_SIGN
import icu.windea.pls.cwt.psi.CwtElementTypes.FLOAT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.INT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.LEFT_BRACE
import icu.windea.pls.cwt.psi.CwtElementTypes.NOT_EQUAL_SIGN
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_COMMENT_START
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_COMMENT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.RIGHT_BRACE
import icu.windea.pls.cwt.psi.CwtElementTypes.STRING_TOKEN
import icu.windea.pls.cwt.editor.CwtAttributesKeyArrays as KA

class CwtSyntaxHighlighter(
    private val project: Project?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?): Array<out TextAttributesKey> {
        return when (tokenType) {
            LEFT_BRACE, RIGHT_BRACE -> KA.BRACES_KEYS
            EQUAL_SIGN, NOT_EQUAL_SIGN -> KA.OPERATOR_KEYS
            DOC_COMMENT_TOKEN -> KA.DOC_COMMENT_KEYS
            OPTION_COMMENT_TOKEN, OPTION_COMMENT_START -> KA.OPTION_COMMENT_KEYS
            COMMENT -> KA.COMMENT_KEYS
            PROPERTY_KEY_TOKEN -> KA.PROPERTY_KEY_KEYS
            OPTION_KEY_TOKEN -> KA.OPTION_KEY_KEYS
            BOOLEAN_TOKEN -> KA.KEYWORD_KEYS
            INT_TOKEN, FLOAT_TOKEN -> KA.NUMBER_KEYS
            STRING_TOKEN -> KA.STRING_KEYS
            VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
            INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
            BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
            else -> KA.EMPTY_KEYS
        }
    }

    override fun getHighlightingLexer(): Lexer {
        return CwtLexerFactory.createHighlightingLexer(project)
    }
}
