package icu.windea.pls.script.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

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
