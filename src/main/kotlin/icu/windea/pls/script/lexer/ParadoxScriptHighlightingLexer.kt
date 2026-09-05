package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.RestartableLexer
import com.intellij.lexer.TokenIterator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.model.ParadoxGameType

@Suppress("UnstableApiUsage")
class ParadoxScriptHighlightingLexer(
    val gameType: ParadoxGameType? = null,
) : LayeredLexer(ParadoxScriptLexer(gameType)), RestartableLexer {
    init {
        ParadoxScriptLexerFactory.registerInlineMathLexer(this, gameType)
        ParadoxScriptLexerFactory.registerLiteralLexer(this)
    }

    // NOTE 3.0.2 implement `RestartableLexer` to optimize lexer reset logic

    override fun getStartState() = 0

    override fun isRestartableState(state: Int): Boolean {
        if (state != 0) return false
        val flex = delegate.castOrNull<ParadoxScriptLexer>()
            ?.delegate?.castOrNull<FlexAdapter>()
            ?.flex?.castOrNull<_ParadoxScriptLexer>()
            ?: return true
        return flex.isRestartable
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
        return start(buffer, startOffset, endOffset, initialState)
    }
}
