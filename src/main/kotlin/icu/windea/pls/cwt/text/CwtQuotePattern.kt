@file:Suppress("unused")

package icu.windea.pls.cwt.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.Cwt: QuotePattern.Base get() = CwtQuotePattern

private const val FORCE_QUOTED_CHARS = "#={}\""

private object CwtQuotePattern : QuotePattern.Base('"') {
    override fun checkChar(text: String, index: Int, char: Char): Boolean {
        // whitespaces are not allowed
        return char.isWhitespace() || char in FORCE_QUOTED_CHARS
    }
}
