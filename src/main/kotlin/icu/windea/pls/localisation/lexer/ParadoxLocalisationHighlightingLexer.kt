package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.RestartableLexer
import com.intellij.lexer.TokenIterator
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.lexer.ParadoxScriptLexer
import icu.windea.pls.script.lexer._ParadoxScriptLexer

@Suppress("UnstableApiUsage")
class ParadoxLocalisationHighlightingLexer(
    val gameType: ParadoxGameType? = null,
) : LayeredLexer(ParadoxLocalisationLexer()), RestartableLexer {
    init {
        ParadoxLocalisationLexerFactory.registerTextLexerWithLiteralLexer(this, gameType)
    }

    // NOTE 3.0.2 implement `RestartableLexer` to optimize lexer reset logic

    override fun getStartState() = 0

    override fun isRestartableState(state: Int): Boolean {
        if (state != 0) return false
        val flex = findLayerLexer(ParadoxLocalisationElementTypes.PROPERTY_VALUE_TOKEN)?.castOrNull<LayeredLexer>()
            ?.delegate?.castOrNull<ParadoxLocalisationTextLexer>()
            ?.delegate?.castOrNull<FlexAdapter>()
            ?.flex?.castOrNull<_ParadoxLocalisationTextLexer>()
            ?: return true
        return flex.isRestartable
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
        return start(buffer, startOffset, endOffset, initialState)
    }
}
