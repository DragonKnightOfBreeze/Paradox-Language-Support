package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationTextLexer(
    val gameType: ParadoxGameType? = null,
) : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationTextLexer(gameType)), ParadoxLocalisationTokenSets.MERGED_TEXT_TOKENS) {
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        super.start(buffer, startOffset, endOffset, initialState)
        val flex = delegate.castOrNull<FlexAdapter>()
            ?.flex?.castOrNull<_ParadoxLocalisationTextLexer>()
            ?: return
        flex.resetContext() // NOTE 3.0.2 should also reset context here
    }
}
