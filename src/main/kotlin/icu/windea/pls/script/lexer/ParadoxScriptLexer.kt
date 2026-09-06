package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.cast
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptLexer(
    val gameType: ParadoxGameType? = null
) : MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer(gameType)), ParadoxScriptTokenSets.MERGED_TOKENS) {
    @Suppress("unused") val flexLexer: _ParadoxScriptLexer get() = delegate.cast<FlexAdapter>().flex.cast()

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        flexLexer.clearContext() // NOTE 3.0.2 it's required to clear the lexer context additionally here
        super.start(buffer, startOffset, endOffset, initialState)
    }
}
