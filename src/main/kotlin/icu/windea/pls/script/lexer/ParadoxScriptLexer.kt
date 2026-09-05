package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.castOrNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptLexer(
    val gameType: ParadoxGameType? = null,
) : MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer(gameType)), ParadoxScriptTokenSets.MERGED_TOKENS) {
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        super.start(buffer, startOffset, endOffset, initialState)
        val flex = delegate.castOrNull<FlexAdapter>()
            ?.flex?.castOrNull<_ParadoxScriptLexer>()
            ?: return
        flex.resetContext() // NOTE 3.0.2 should also reset context here
    }
}
