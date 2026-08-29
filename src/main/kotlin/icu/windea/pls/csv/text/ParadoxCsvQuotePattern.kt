@file:Suppress("unused")

package icu.windea.pls.csv.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxCsv: QuotePattern.Base get() = ParadoxCsvQuotePattern

// TODO 3.0.2 refactor

private const val FORCE_QUOTED_CHARS = "#;\""

private object ParadoxCsvQuotePattern : QuotePattern.Base('"') {
    override fun checkChar(text: String, index: Int, char: Char): Boolean {
        // whitespaces are allowed
        return char in FORCE_QUOTED_CHARS
    }
}
