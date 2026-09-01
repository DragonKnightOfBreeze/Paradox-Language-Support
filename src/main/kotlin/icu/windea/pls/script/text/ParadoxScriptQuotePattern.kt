@file:Suppress("unused")

package icu.windea.pls.script.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxScript: QuotePattern.Base get() = ParadoxScriptQuotePattern

private object ParadoxScriptQuotePattern : QuotePattern.Base('"') {
    // NOTE 3.0.2 for `needQuote`, need to check boundary characters specially due to low-level lexer implementation

    private const val FORCE_QUOTED_CHARS = "#=<>{}\"" // include `$[]` & `@!?` are allowed
    private const val FORCE_QUOTED_BOUND_CHARS = "#=<>{}\"@!?" // include `$[]` & `@!?` are not allowed

    override fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char): Boolean {
        return when {
            index == start || index == end -> char.isWhitespace() || char in FORCE_QUOTED_BOUND_CHARS
            else -> char.isWhitespace() || char in FORCE_QUOTED_CHARS
        }
    }
}
