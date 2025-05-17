package icu.windea.pls.script.lexer

import com.intellij.lexer.*
import com.intellij.openapi.project.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptLexerFactory {
    @JvmStatic
    fun createLexer(project: Project? = null): ParadoxScriptLexer {
        return ParadoxScriptLexer()
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        val literalTypes = ParadoxScriptTokenSets.LITERAL_TOKENS.types
        literalTypes.forEach { lexer.registerSelfStoppingLayer(createStringLiteralLexer(it), arrayOf(it), IElementType.EMPTY_ARRAY) }
        return lexer
    }

    @JvmStatic
    fun createStringLiteralLexer(elementType: IElementType): StringLiteralLexer {
        return ParadoxScriptStringLiteralLexer(elementType)
    }
}
