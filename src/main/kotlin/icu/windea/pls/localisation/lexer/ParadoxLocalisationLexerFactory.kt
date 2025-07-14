package icu.windea.pls.localisation.lexer

import com.intellij.lexer.*
import com.intellij.openapi.project.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationLexerFactory {
    @JvmStatic
    fun createLexer(project: Project? = null): ParadoxLocalisationLexer {
        return ParadoxLocalisationLexer()
    }

    @JvmStatic
    fun createTextLexer(project: Project? = null, gameType: ParadoxGameType? = null): ParadoxLocalisationTextLexer {
        return ParadoxLocalisationTextLexer(gameType)
    }

    @JvmStatic
    fun createLayeredLexer(project: Project? = null, gameType: ParadoxGameType? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        val textLexer = LayeredLexer(createTextLexer(project, gameType))
        lexer.registerSelfStoppingLayer(textLexer, arrayOf(PROPERTY_VALUE_TOKEN), emptyArray())
        return lexer
    }

    @JvmStatic
    fun createHighlightingLexer(project: Project? = null, gameType: ParadoxGameType? = null): LayeredLexer {
        val lexer = LayeredLexer(createLexer(project))
        val textLexer = LayeredLexer(createTextLexer(project, gameType))
        lexer.registerSelfStoppingLayer(textLexer, arrayOf(PROPERTY_VALUE_TOKEN), IElementType.EMPTY_ARRAY)
        textLexer.registerSelfStoppingLayer(createStringLiteralLexer(STRING_TOKEN), arrayOf(STRING_TOKEN), IElementType.EMPTY_ARRAY)
        return lexer
    }

    @JvmStatic
    fun createStringLiteralLexer(elementType: IElementType): StringLiteralLexer {
        return ParadoxLocalisationStringLiteralLexer(elementType)
    }
}
