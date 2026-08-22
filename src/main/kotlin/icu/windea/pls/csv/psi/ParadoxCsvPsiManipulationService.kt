package icu.windea.pls.csv.psi

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.cast
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote

object ParadoxCsvPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#;\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it in FORCE_QUOTED_CHARS } // whitespaces are allowed
    }

    fun quoteIfNeeded(expression: String): String {
        if (expression.isLeftQuoted() && expression.isRightQuoted()) return expression
        if (!needQuote(expression)) return expression
        return expression.unquote().quote() // unquote first
    }

    fun changeContent(element: ParadoxCsvColumn, newContent: String, range: TextRange? = null): ParadoxCsvColumn {
        val newValue = range?.replace(element.text, newContent) ?: newContent
        val newText = quoteIfNeeded(newValue)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}
