package icu.windea.pls.cwt.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.psi.CwtElementTypes.*

@Suppress("UNUSED_PARAMETER")
object CwtLexerFactory {
    private val propertyKeyTokens = arrayOf(PROPERTY_KEY_TOKEN)
    private val stringTokens = arrayOf(STRING_TOKEN)
    private val emptyTokens = IElementType.EMPTY_ARRAY

    @JvmStatic
    fun createLexer(project: Project? = null): CwtLexer {
        return CwtLexer()
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null): CwtHighlightingLexer {
        return CwtHighlightingLexer()
    }

    @JvmStatic
    fun registerLiteralLexer(lexer: LayeredLexer) {
        lexer.registerSelfStoppingLayer(CwtStringLiteralLexer(PROPERTY_KEY_TOKEN), propertyKeyTokens, emptyTokens)
        lexer.registerSelfStoppingLayer(CwtStringLiteralLexer(STRING_TOKEN), stringTokens, emptyTokens)
    }
}
