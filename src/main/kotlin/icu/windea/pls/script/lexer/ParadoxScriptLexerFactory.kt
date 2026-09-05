package icu.windea.pls.script.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.cwt.lexer.CwtStringLiteralLexer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING_TOKEN

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptLexerFactory {
    private val inlineMathTokens = arrayOf(INLINE_MATH_TOKEN)
    private val propertyKeyTokens = arrayOf(PROPERTY_KEY_TOKEN)
    private val stringTokens = arrayOf(STRING_TOKEN)
    private val emptyTokens = IElementType.EMPTY_ARRAY

    @JvmStatic
    fun createLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxScriptLexer {
        // NOTE 3.0.2 `gameType` is unused (so the argument is not passed) atm
        return ParadoxScriptLexer(gameType)
    }

    @JvmStatic
    fun createInlineMathLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxScriptInlineMathLexer {
        // NOTE 3.0.2 `gameType` is unused (so the argument is not passed) atm
        return ParadoxScriptInlineMathLexer(gameType)
    }

    @JvmStatic
    fun createLayeredLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxScriptLayeredLexer {
        // NOTE 3.0.2 `gameType` is unused (so the argument is not passed) atm
        return ParadoxScriptLayeredLexer(gameType)
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null,gameType: ParadoxGameType? = null): ParadoxScriptHighlightingLexer {
        // NOTE 3.0.2 `gameType` is unused (so the argument is not passed) atm
        return ParadoxScriptHighlightingLexer(gameType)
    }

    @JvmStatic
     fun registerInlineMathLexer(lexer: LayeredLexer, gameType: ParadoxGameType? = null) {
        lexer.registerSelfStoppingLayer(ParadoxScriptInlineMathLexer(gameType), inlineMathTokens, emptyTokens)
    }

    @JvmStatic
     fun registerLiteralLexer(lexer: LayeredLexer) {
        lexer.registerSelfStoppingLayer(CwtStringLiteralLexer(PROPERTY_KEY_TOKEN), propertyKeyTokens, emptyTokens)
        lexer.registerSelfStoppingLayer(CwtStringLiteralLexer(STRING_TOKEN), stringTokens, emptyTokens)
    }
}
