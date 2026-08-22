package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.replaceAndQuoteIfNeeded

object CwtPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#={}\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        return if (!expression.isQuoted() && needQuote(expression)) expression.quote() else expression
    }

    fun changeContent(element: CwtOptionKey, range: TextRange, newContent: String): CwtOptionKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = CwtElementFactory.createOptionKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtPropertyKey, range: TextRange, newContent: String): CwtPropertyKey {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = CwtElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtValue, range: TextRange, newContent: String): CwtValue {
        if (element is CwtString) return changeContent(element, range, newContent)

        val text = element.text
        val newText = range.replace(text, newContent)
        val newElement = CwtElementFactory.createValueFromText(element.project, newText)
        return element.replace(newElement).cast()
    }

    fun changeContent(element: CwtString, range: TextRange, newContent: String): CwtString {
        val text = element.text
        val newText = range.replaceAndQuoteIfNeeded(text, newContent)
        val newElement = CwtElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
