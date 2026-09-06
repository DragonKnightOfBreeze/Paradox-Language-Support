package icu.windea.pls.cwt.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.core.cast
import icu.windea.pls.cwt.psi.CwtTokenSets

class CwtLexer : MergingLexerAdapter(FlexAdapter(_CwtLexer()), CwtTokenSets.MERGED_TOKENS) {
    @Suppress("unused") val flexLexer: _CwtLexer get() = delegate.cast<FlexAdapter>().flex.cast()
}
