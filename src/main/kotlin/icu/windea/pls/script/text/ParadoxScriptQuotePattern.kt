@file:Suppress("unused")

package icu.windea.pls.script.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxScript: QuotePattern.Base get() = ParadoxScriptQuotePattern

// TODO 3.0.2 refactor

private const val FORCE_QUOTED_CHARS = "@#=<>!?{}\""

private object ParadoxScriptQuotePattern : QuotePattern.Base('"') {
    override fun checkChar(text: String, index: Int, char: Char): Boolean {
        // whitespaces are not allowed
        return char.isWhitespace() || char in FORCE_QUOTED_CHARS
    }
}
