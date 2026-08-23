@file:Suppress("unused")

package icu.windea.pls.script.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxScript: QuotePattern.Base get() = ParadoxScriptQuotePattern

private object ParadoxScriptQuotePattern : QuotePattern.Base('"') {
    private const val FORCE_QUOTED_CHARS = "@#=<>!?{}[\""

    override fun checkUnquotedChar(char: Char): Boolean {
        // whitespaces are not allowed
        return char.isWhitespace() || char in FORCE_QUOTED_CHARS
    }
}
