@file:Suppress("unused")

package icu.windea.pls.expression.text

import icu.windea.pls.core.isIdentifierChar
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

@Suppress("UnusedReceiverParameter")
val QuotePatterns.ParadoxLiteralNode: QuotePattern.Base get() = ParadoxLiteralNodeQuotePattern

private object ParadoxLiteralNodeQuotePattern : QuotePattern.Base('\'') { // use single quote here
    override fun checkChar(text: String, index: Int, char: Char): Boolean {
        // whitespaces are not allowed + (lenient check) non-identifier chars are not allowed
        return char.isWhitespace() || !char.isIdentifierChar()
    }
}
