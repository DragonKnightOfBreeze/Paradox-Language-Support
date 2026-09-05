package icu.windea.pls.localisation.lexer

import com.intellij.lexer.LayeredLexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.ParadoxGameType

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationLexerFactory {
    private val propertyValueTokens = arrayOf(PROPERTY_VALUE_TOKEN)
    private val textTokens = arrayOf(TEXT_TOKEN)
    private val emptyTokens = IElementType.EMPTY_ARRAY

    @JvmStatic
    fun createLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxLocalisationLexer {
        // NOTE 3.0.2 `gameType` is unused (so the argument is not passed) atm
        return ParadoxLocalisationLexer(gameType)
    }

    @JvmStatic
    fun createTextLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxLocalisationTextLexer {
        return ParadoxLocalisationTextLexer(gameType)
    }

    @JvmStatic
    fun createLayeredLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxLocalisationLayeredLexer {
        return ParadoxLocalisationLayeredLexer(gameType)
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxLocalisationHighlightingLexer {
        return ParadoxLocalisationHighlightingLexer(gameType)
    }

    @JvmStatic
    fun registerTextLexer(lexer: LayeredLexer, gameType: ParadoxGameType?) {
        val textLexer = ParadoxLocalisationTextLexer(gameType)
        lexer.registerSelfStoppingLayer(textLexer, propertyValueTokens, emptyTokens)
    }

    @JvmStatic
    fun registerTextLexerWithLiteralLexer(lexer: LayeredLexer, gameType: ParadoxGameType?) {
        val textLexer = LayeredLexer(ParadoxLocalisationTextLexer(gameType))
        lexer.registerSelfStoppingLayer(textLexer, propertyValueTokens, emptyTokens)
        textLexer.registerSelfStoppingLayer(ParadoxLocalisationTextLiteralLexer(TEXT_TOKEN), textTokens, emptyTokens)
    }
}
