package icu.windea.pls.cwt.lexer

import com.intellij.lexer.*
import com.intellij.openapi.project.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("UNUSED_PARAMETER")
object CwtLexerFactory {
    @JvmStatic
    fun createLexer(project: Project? = null): CwtLexer {
        return CwtLexer()
    }

    @JvmStatic
    fun createOptionCommentLexer(project: Project? = null): CwtOptionCommentLexer {
        return CwtOptionCommentLexer()
    }

    @JvmStatic
    fun createLayeredLexer(project: Project? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        val optionCommentLexer = LayeredLexer(createOptionCommentLexer(project))
        lexer.registerSelfStoppingLayer(optionCommentLexer, arrayOf(OPTION_COMMENT_TOKEN), emptyArray())
        return lexer
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        val literalTypes = CwtTokenSets.LITERAL_TOKENS.types
        literalTypes.forEach { lexer.registerSelfStoppingLayer(createStringLiteralLexer(it), arrayOf(it), IElementType.EMPTY_ARRAY) }
        val optionCommentLexer = LayeredLexer(createOptionCommentLexer(project))
        literalTypes.forEach { optionCommentLexer.registerSelfStoppingLayer(createStringLiteralLexer(it), arrayOf(it), IElementType.EMPTY_ARRAY) }
        lexer.registerSelfStoppingLayer(optionCommentLexer, arrayOf(OPTION_COMMENT_TOKEN), IElementType.EMPTY_ARRAY)
        return lexer
    }

    @JvmStatic
    fun createStringLiteralLexer(elementType: IElementType): StringLiteralLexer {
        return CwtStringLiteralLexer(elementType)
    }
}
