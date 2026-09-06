package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.cast
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationLexer(
    val gameType: ParadoxGameType? = null
) : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer(gameType)), ParadoxLocalisationTokenSets.MERGED_TOKENS) {
    @Suppress("unused") val flexLexer: _ParadoxLocalisationLexer get() = delegate.cast<FlexAdapter>().flex.cast()
}
