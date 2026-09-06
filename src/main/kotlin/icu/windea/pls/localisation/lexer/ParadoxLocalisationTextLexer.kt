package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.cast
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationTextLexer(
    val gameType: ParadoxGameType? = null
) : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationTextLexer(gameType)), ParadoxLocalisationTokenSets.MERGED_TEXT_TOKENS) {
    @Suppress("unused")  val flexLexer: _ParadoxLocalisationTextLexer get() = delegate.cast<FlexAdapter>().flex.cast()

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        flexLexer.clearContext() // NOTE 3.0.2 it's required to clear the lexer context additionally here
        super.start(buffer, startOffset, endOffset, initialState)
    }
}
