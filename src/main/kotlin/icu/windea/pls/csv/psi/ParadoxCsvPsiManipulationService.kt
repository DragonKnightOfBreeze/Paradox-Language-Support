package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.quote
import icu.windea.pls.core.replaceAndQuoteIfNeeded

object ParadoxCsvPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#;\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it in FORCE_QUOTED_CHARS } // whitespaces are allowed
    }

    fun quoteIfNeeded(expression: String): String {
        return if (needQuote(expression)) expression.quote() else expression
    }

    fun changeContent(element: ParadoxCsvColumn, range: TextRange, newContent: String): ParadoxCsvColumn {
        val text = element.text
        val extraChars = ParadoxCsvPsiService.getSeparator().toString()
        val newText = range.replaceAndQuoteIfNeeded(text, newContent, containAnyChar = extraChars, containBlank = false)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
