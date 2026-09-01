@file:Suppress("unused")

package icu.windea.pls.localisation.text

import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxLocalisation: QuotePattern.Base get() = ParadoxLocalisationQuotePattern

private object ParadoxLocalisationQuotePattern : QuotePattern.Base('"') {
    override fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char): Boolean {
        // always false (except for line breaks, but that are unexpected)
        return false
    }
}
