@file:Suppress("unused")

package icu.windea.pls.csv.text

import icu.windea.pls.core.text.QuotePattern

@Suppress("UnusedReceiverParameter")
val QuotePattern.ParadoxCsv: QuotePattern.Base get() = ParadoxCsvQuotePattern

private object ParadoxCsvQuotePattern : QuotePattern.Base('"') {
    private const val FORCE_QUOTED_CHARS = "#;\""

    override fun checkUnquotedChar(char: Char): Boolean {
        // whitespaces are allowed
        return char in FORCE_QUOTED_CHARS
    }
}
