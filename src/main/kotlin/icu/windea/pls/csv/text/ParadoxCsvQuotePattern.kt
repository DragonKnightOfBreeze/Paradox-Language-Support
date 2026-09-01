@file:Suppress("unused")

package icu.windea.pls.csv.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxCsv: QuotePattern.Base get() = ParadoxCsvQuotePattern

private object ParadoxCsvQuotePattern : QuotePattern.Base('"') {
    // NOTE 3.0.2 for `needQuote`, need to check boundary characters specially due to low-level lexer implementation

    private const val FORCE_QUOTED_CHARS = "#;\""
    private const val FORCE_QUOTED_BOUND_CHARS = "#;\""

    override fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char): Boolean {
        return when {
            index == start || index == end -> char.isWhitespace() || char in FORCE_QUOTED_BOUND_CHARS
            else -> /* char.isWhitespace() ||  */char in FORCE_QUOTED_CHARS // whitespaces are allowed here
        }
    }
}
