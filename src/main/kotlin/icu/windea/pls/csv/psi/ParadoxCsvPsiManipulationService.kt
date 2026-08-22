package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.quote

object ParadoxCsvPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#;\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it in FORCE_QUOTED_CHARS } // whitespaces are allowed
    }

    fun quoteIfNeeded(expression: String): String {
        return if (needQuote(expression)) expression.quote() else expression
    }

    fun changeContent(element: ParadoxCsvColumn, newContent: String, range: TextRange? = null): ParadoxCsvColumn {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newElement = ParadoxCsvElementFactory.createColumn(element.project, newValue)
        return element.replace(newElement).cast()
    }
}
